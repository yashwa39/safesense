// True when running on GitHub Pages or opened as a local file — no backend available.
const STATIC_ONLY = location.protocol === "file:" || location.hostname.endsWith("github.io");

const STATUS_LABEL = {
  NORMAL: "Normal",
  WARNING: "Warning",
  CRITICAL: "Critical",
  SENSOR_ERROR: "Sensor error"
};

const ADVICE = {
  NORMAL: "What to do: no action needed.",
  WARNING: "What to do: check ventilation and monitor closely.",
  CRITICAL: "What to do: leave the affected area now and tell your supervisor."
};

const historyBySensor = {};
const seenToasts = new Set();
let chart;
let selectedSensor = "Sound level";

const els = {
  hello: document.getElementById("hello"),
  updated: document.getElementById("updated"),
  banner: document.getElementById("banner"),
  cards: document.getElementById("cards"),
  pill: document.getElementById("overall-pill"),
  alerts: document.getElementById("alert-list"),
  badge: document.getElementById("alert-badge"),
  toast: document.getElementById("toast"),
  conn: document.getElementById("conn-banner"),
  chartSelect: document.getElementById("chart-sensor"),
  userName: document.getElementById("user-name"),
  userRole: document.getElementById("user-role"),
  avatar: document.getElementById("avatar")
};

document.getElementById("menu-toggle").addEventListener("click", () => {
  const sidebar = document.getElementById("sidebar");
  const open = sidebar.classList.toggle("open");
  document.getElementById("menu-toggle").setAttribute("aria-expanded", String(open));
});

document.querySelectorAll(".segmented button[data-mode]").forEach((button) => {
  button.addEventListener("click", async () => {
    const mode = button.getAttribute("data-mode");
    if (!STATIC_ONLY) {
      try {
        await fetch("api/demo-mode?mode=" + encodeURIComponent(mode), { method: "POST" });
      } catch (err) {
        // Backend unreachable — simulator handles the mode change below.
      }
    }
    SafeSenseSim.setMode(mode);
    document.querySelectorAll(".segmented button[data-mode]").forEach((b) => {
      b.setAttribute("aria-pressed", String(b === button));
    });
    poll();
  });
});

function statusText(status) {
  return STATUS_LABEL[status] || status;
}

function renderBanner(data) {
  const overall = data.overallStatus;
  els.banner.className = "banner " + overall;
  const offenders = data.sensors.filter((s) => s.status === "WARNING" || s.status === "CRITICAL");
  let html = "";
  if (overall === "NORMAL") {
    html = "<strong>All readings are within safe limits.</strong>";
  } else {
    html = "<strong>" + offenders.length + " readings are above safe limits</strong><ul>";
    offenders.forEach((s) => {
      html += "<li>" + s.message + "</li>";
    });
    html += "</ul>";
  }
  html += "<p>" + (ADVICE[overall] || "") + "</p>";
  html += '<div class="banner-actions"><button class="btn secondary" type="button" id="view-alerts">View alert details</button></div>';
  els.banner.innerHTML = html;
  document.getElementById("view-alerts").addEventListener("click", () => {
    document.getElementById("recent-alerts").scrollIntoView({ behavior: "smooth" });
  });
}

function iconFor(name) {
  if (name.includes("Sound")) {
    return '<svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M4 10v4h4l5 5V5L8 10H4zm13 2a3 3 0 0 0-3-3v6a3 3 0 0 0 3-3z"/></svg>';
  }
  return '<svg width="18" height="18" viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a5 5 0 0 1 5 5v3h1a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2h1V7a5 5 0 0 1 5-5z"/></svg>';
}

function barHtml(sensor) {
  const max = Math.max(sensor.criticalLimit * 1.2, (sensor.value || 0) * 1.05, 1);
  const warn = (sensor.warningLimit / max) * 100;
  const crit = ((sensor.criticalLimit - sensor.warningLimit) / max) * 100;
  const rest = 100 - warn - crit;
  const pos = sensor.error ? 0 : Math.min(100, ((sensor.value || 0) / max) * 100);
  return '<div class="bar" aria-hidden="true"><span class="g" style="width:' + warn + '%"></span><span class="a" style="width:' + crit + '%"></span><span class="r" style="width:' + rest + '%"></span><span class="marker" style="left:' + pos + '%"></span></div>';
}

function renderCards(sensors) {
  els.cards.innerHTML = sensors.map((sensor) => {
    const value = sensor.error || sensor.value == null ? "--" : sensor.value + " " + sensor.unit;
    return '<article class="card">' +
      '<div class="card-head">' + iconFor(sensor.name) + "<span>" + sensor.name + "</span></div>" +
      '<div class="value">' + value + "</div>" +
      barHtml(sensor) +
      '<span class="chip ' + sensor.status + '"><span class="dot"></span>' + statusText(sensor.status) + "</span>" +
      '<div class="footer-limits">Limits ' + sensor.warningLimit + " / " + sensor.criticalLimit + "</div>" +
      "</article>";
  }).join("");
}

function renderAlerts(alerts) {
  const latest = alerts.slice(0, 5);
  if (!latest.length) {
    els.alerts.innerHTML = "<p class=\"muted\">No alerts yet</p>";
    return;
  }
  els.alerts.innerHTML = latest.map((alert) => {
    const time = new Date(alert.timestamp).toLocaleTimeString();
    return '<div class="alert-item"><span class="muted">' + time + "</span><span>" +
      alert.sensorName + " · " + alert.value + " " + (alert.unit || "") +
      '</span><span class="chip ' + alert.status + '"><span class="dot"></span>' +
      statusText(alert.status) + "</span></div>";
  }).join("");
}

function updateChart(sensors) {
  if (!els.chartSelect.options.length) {
    els.chartSelect.innerHTML = sensors.map((s) =>
      '<option value="' + s.name + '">' + s.name + "</option>"
    ).join("");
    els.chartSelect.value = selectedSensor;
    els.chartSelect.addEventListener("change", () => {
      selectedSensor = els.chartSelect.value;
      drawChart(sensors);
    });
  }
  sensors.forEach((sensor) => {
    if (!historyBySensor[sensor.name]) {
      historyBySensor[sensor.name] = [];
    }
    const series = historyBySensor[sensor.name];
    series.push({ t: Date.now(), v: sensor.error ? null : sensor.value });
    while (series.length > 30) {
      series.shift();
    }
  });
  drawChart(sensors);
}

function drawChart(sensors) {
  const sensor = sensors.find((s) => s.name === selectedSensor) || sensors[0];
  const series = historyBySensor[sensor.name] || [];
  const ctx = document.getElementById("chart");
  const labels = series.map((_, i) => (series.length - i - 1) * -2 + "s");
  const data = {
    labels,
    datasets: [
      {
        label: sensor.name,
        data: series.map((p) => p.v),
        borderColor: "#2748D8",
        backgroundColor: "transparent",
        tension: 0.25,
        pointRadius: 0
      },
      {
        label: "Warning " + sensor.warningLimit,
        data: series.map(() => sensor.warningLimit),
        borderColor: "#B7791F",
        borderDash: [6, 4],
        pointRadius: 0
      },
      {
        label: "Critical " + sensor.criticalLimit,
        data: series.map(() => sensor.criticalLimit),
        borderColor: "#C62828",
        borderDash: [6, 4],
        pointRadius: 0
      }
    ]
  };
  if (chart) {
    chart.data = data;
    chart.update("none");
    return;
  }
  chart = new Chart(ctx, {
    type: "line",
    data,
    options: {
      animation: false,
      plugins: { legend: { display: true } },
      scales: { y: { beginAtZero: true } }
    }
  });
}

function showToasts(messages) {
  messages.forEach((message) => {
    const key = message + Date.now();
    if (seenToasts.has(message) && Date.now() - (showToasts.last || 0) < 1500) {
      return;
    }
    seenToasts.add(message);
    els.toast.hidden = false;
    els.toast.textContent = "Critical alert: " + message;
    showToasts.last = Date.now();
    setTimeout(() => {
      els.toast.hidden = true;
    }, 5000);
  });
}

async function fetchJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error("bad status");
  }
  return response.json();
}

function applySnapshot(data, alertsBody) {
  els.conn.classList.remove("show");
  els.updated.textContent = "updated just now";
  els.pill.className = "pill " + data.overallStatus;
  els.pill.textContent = statusText(data.overallStatus);
  renderBanner(data);
  renderCards(data.sensors);
  updateChart(data.sensors);
  renderAlerts(alertsBody.alerts || []);
  const count = alertsBody.count || 0;
  els.badge.hidden = count === 0;
  els.badge.textContent = String(count);
  document.querySelectorAll(".segmented button[data-mode]").forEach((b) => {
    b.setAttribute("aria-pressed", String(b.getAttribute("data-mode") === data.demoMode));
  });
  showToasts(data.newCriticalMessages || []);
}

let apiWasReachable = false;

async function poll() {
  if (STATIC_ONLY) {
    applySnapshot(SafeSenseSim.tick(), SafeSenseSim.getAlerts());
    return;
  }
  try {
    const [data, alertsBody] = await Promise.all([
      fetchJson("api/readings"),
      fetchJson("api/alerts")
    ]);
    apiWasReachable = true;
    applySnapshot(data, alertsBody);
  } catch (err) {
    if (apiWasReachable) {
      els.conn.classList.add("show");
      return;
    }
    applySnapshot(SafeSenseSim.tick(), SafeSenseSim.getAlerts());
  }
}

poll();
setInterval(poll, 2000);
