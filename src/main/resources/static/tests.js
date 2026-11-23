let report = null;
let liveReport = null;
let usingLive = false;
let category = "All";
let search = "";
let expanded = null;

const CATEGORIES = ["All", "Unit", "Threshold", "Dashboard", "Alerts", "Login", "Edge", "API"];
const CONCEPTS = [
  "Encapsulation",
  "Inheritance",
  "Method overriding",
  "Polymorphism",
  "Exception handling",
  "Interface"
];

const els = {
  subtitle: document.getElementById("subtitle"),
  env: document.getElementById("env-line"),
  summary: document.getElementById("summary"),
  stack: document.getElementById("stack-bar"),
  filters: document.getElementById("filters"),
  rows: document.getElementById("rows"),
  coverage: document.getElementById("oop-coverage"),
  run: document.getElementById("run-live"),
  download: document.getElementById("download"),
  search: document.getElementById("search"),
  sample: document.getElementById("toggle-sample"),
  live: document.getElementById("toggle-live")
};

document.getElementById("menu-toggle").addEventListener("click", () => {
  document.getElementById("sidebar").classList.toggle("open");
});
document.getElementById("logout").addEventListener("click", async (event) => {
  event.preventDefault();
  await fetch("/api/logout", { method: "POST", credentials: "same-origin" });
  window.location.href = "/login";
});

els.sample.addEventListener("click", () => {
  usingLive = false;
  els.sample.setAttribute("aria-pressed", "true");
  els.live.setAttribute("aria-pressed", "false");
  render();
});
els.live.addEventListener("click", () => {
  usingLive = true;
  els.sample.setAttribute("aria-pressed", "false");
  els.live.setAttribute("aria-pressed", "true");
  render();
});
els.search.addEventListener("input", () => {
  search = els.search.value.trim().toLowerCase();
  renderTable();
});
els.run.addEventListener("click", runLive);
els.download.addEventListener("click", () => {
  const current = currentReport();
  if (!current) {
    return;
  }
  const blob = new Blob([JSON.stringify(current, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = usingLive ? "safesense-live-results.json" : "safesense-sample-results.json";
  a.click();
  URL.revokeObjectURL(url);
});

function currentReport() {
  return usingLive ? liveReport : report;
}

function chip(result) {
  const cls = result === "PASS" ? "NORMAL" : result === "FIXED" ? "WARNING" : "CRITICAL";
  return '<span class="chip ' + cls + '"><span class="dot"></span>' + result + "</span>";
}

function renderSummary(data) {
  const s = data.summary || {};
  const cards = [
    ["Total", s.total ?? 0],
    ["Passed", s.passed ?? 0],
    ["Fixed after retest", s.fixed ?? 0],
    ["Pass rate (%)", s.passRate ?? 0]
  ];
  els.summary.innerHTML = cards.map(([label, value]) =>
    '<article class="card"><div class="muted">' + label + '</div><div class="stat">' + value + "</div></article>"
  ).join("");
  const total = s.total || 1;
  const passW = ((s.passed || 0) / total) * 100;
  const fixedW = ((s.fixed || 0) / total) * 100;
  const failW = ((s.failed || 0) / total) * 100;
  els.stack.innerHTML =
    '<span class="pass" style="width:' + passW + '%"></span>' +
    '<span class="fixed" style="width:' + fixedW + '%"></span>' +
    '<span class="fail" style="width:' + failW + '%"></span>';
}

function renderFilters(data) {
  const counts = (data.summary && data.summary.byCategory) || {};
  const total = data.tests ? data.tests.length : 0;
  els.filters.innerHTML = CATEGORIES.map((name) => {
    const count = name === "All" ? total : (counts[name] || 0);
    const pressed = category === name;
    return '<button type="button" data-cat="' + name + '" aria-pressed="' + pressed + '">' +
      name + " (" + count + ")</button>";
  }).join("");
  els.filters.querySelectorAll("button").forEach((button) => {
    button.addEventListener("click", () => {
      category = button.getAttribute("data-cat");
      render();
    });
  });
}

function filteredTests(data) {
  return (data.tests || []).filter((test) => {
    const catOk = category === "All" || test.category === category;
    const q = search;
    const textOk = !q || (test.id || "").toLowerCase().includes(q) || (test.name || "").toLowerCase().includes(q);
    return catOk && textOk;
  });
}

function detailsHtml(test, data) {
  let html = "<p><strong>Category:</strong> " + test.category + "</p>" +
    "<p><strong>Expected:</strong> " + test.expected + "</p>" +
    "<p><strong>Actual:</strong> " + test.actual + "</p>";
  if (test.id === "T8" && data.bug) {
    html += '<div class="bug-card"><strong>' + data.bug.id + "</strong>" +
      "<p><strong>Symptom:</strong> " + data.bug.symptom + "</p>" +
      "<p><strong>Root cause:</strong> " + data.bug.rootCause + "</p>" +
      "<p><strong>Fix:</strong> " + data.bug.fix + "</p>" +
      "<p><strong>Retest:</strong> " + data.bug.retest + "</p></div>";
  }
  return html;
}

function renderTable() {
  const data = currentReport();
  if (!data) {
    els.rows.innerHTML = '<tr><td colspan="8">No live results yet. Click Run live tests.</td></tr>';
    return;
  }
  const tests = filteredTests(data);
  els.rows.innerHTML = tests.map((test) => {
    const open = expanded === test.id;
    let row = '<tr class="expandable" tabindex="0" data-id="' + test.id + '">' +
      "<td>" + test.id + "</td>" +
      "<td>" + test.name + "</td>" +
      "<td>" + (test.input || "") + "</td>" +
      "<td>" + (test.expected || "") + "</td>" +
      "<td>" + (test.actual || "") + "</td>" +
      "<td>" + (test.oopConcept ? '<span class="concept">' + test.oopConcept + "</span>" : "—") + "</td>" +
      "<td>" + (test.durationMs != null ? test.durationMs + " ms" : "—") + "</td>" +
      "<td>" + chip(test.result) + "</td></tr>";
    if (open) {
      row += '<tr class="details"><td colspan="8">' + detailsHtml(test, data) + "</td></tr>";
    }
    return row;
  }).join("");
  els.rows.querySelectorAll("tr.expandable").forEach((row) => {
    const toggle = () => {
      const id = row.getAttribute("data-id");
      expanded = expanded === id ? null : id;
      renderTable();
      const next = els.rows.querySelector('tr[data-id="' + id + '"]');
      if (next) {
        next.focus();
      }
    };
    row.addEventListener("click", toggle);
    row.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        toggle();
      }
    });
  });
}

function renderCoverage(data) {
  const tests = data && data.tests ? data.tests : [];
  const groups = {};
  CONCEPTS.forEach((c) => {
    groups[c] = [];
  });
  tests.forEach((test) => {
    if (!test.oopConcept) {
      return;
    }
    const key = CONCEPTS.find((c) => c.toLowerCase() === test.oopConcept.toLowerCase());
    if (key) {
      groups[key].push(test.id);
    }
  });
  els.coverage.innerHTML = CONCEPTS.map((c) =>
    "<p><strong>" + c + "</strong> — " + groups[c].length + " test(s): " +
    (groups[c].length ? groups[c].join(", ") : "none in this report") + "</p>"
  ).join("");
}

function render() {
  const data = currentReport();
  if (!data) {
    els.subtitle.textContent = "Live run — click Run live tests";
    els.env.textContent = "";
    els.summary.innerHTML = "";
    els.stack.innerHTML = "";
    renderFilters({ tests: [], summary: { byCategory: {} } });
    renderTable();
    renderCoverage({ tests: [] });
    return;
  }
  els.subtitle.textContent = data.note || "";
  els.env.textContent = [data.build, data.environment, data.course, data.team].filter(Boolean).join(" · ");
  renderSummary(data);
  renderFilters(data);
  renderTable();
  renderCoverage(data);
}

async function runLive() {
  els.run.disabled = true;
  els.run.innerHTML = '<span class="spinner" aria-hidden="true"></span>Running';
  try {
    const response = await fetch("/api/test-results/run", {
      method: "POST",
      credentials: "same-origin"
    });
    liveReport = await response.json();
    usingLive = true;
    els.sample.setAttribute("aria-pressed", "false");
    els.live.setAttribute("aria-pressed", "true");
    render();
  } finally {
    els.run.disabled = false;
    els.run.textContent = "Run live tests";
  }
}

fetch("/api/test-results", { credentials: "same-origin" })
  .then((r) => {
    if (r.status === 401) {
      window.location.href = "/login";
      throw new Error("auth");
    }
    return r.json();
  })
  .then((data) => {
    report = data;
    render();
  });
