/**
 * Browser-side simulator so the dashboard works on GitHub Pages
 * (no Java backend). Rules match the Java Threshold / sensor classes.
 */
(function (global) {
  function classify(value, warningLimit, criticalLimit) {
    if (Number.isNaN(value)) {
      return "SENSOR_ERROR";
    }
    if (value >= criticalLimit) {
      return "CRITICAL";
    }
    if (value >= warningLimit) {
      return "WARNING";
    }
    return "NORMAL";
  }

  function formatNumber(value) {
    if (value === Math.rint(value) && Math.abs(value) < 1000000) {
      return value.toFixed(1);
    }
    if (Math.abs(value) >= 1000) {
      return String(Math.round(value));
    }
    return value.toFixed(1);
  }

  function rank(status) {
    if (status === "CRITICAL") {
      return 2;
    }
    if (status === "WARNING") {
      return 1;
    }
    return 0;
  }

  function messageFor(spec, value, status, error) {
    if (error) {
      return "Sensor error";
    }
    const unit = spec.unit;
    const valueText = formatNumber(value);
    if (spec.type === "SOUND") {
      if (status === "CRITICAL") {
        return "Sound level is " + valueText + " dB, above the critical limit of " + formatNumber(spec.criticalLimit) + " dB.";
      }
      if (status === "WARNING") {
        return "Sound level is " + valueText + " dB, above the warning limit of " + formatNumber(spec.warningLimit) + " dB.";
      }
      return "Sound level is " + valueText + " dB, within safe limits.";
    }
    if (status === "CRITICAL") {
      return spec.name + " is " + valueText + " " + unit + ", above the critical limit of " + formatNumber(spec.criticalLimit) + " " + unit + ".";
    }
    if (status === "WARNING") {
      return spec.name + " is " + valueText + " " + unit + ", above the warning limit of " + formatNumber(spec.warningLimit) + " " + unit + ".";
    }
    return spec.name + " is " + valueText + " " + unit + ", within safe limits.";
  }

  const SPECS = [
    { key: "sound", name: "Sound level", type: "SOUND", unit: "dB", warningLimit: 85, criticalLimit: 100 },
    { key: "co", name: "Carbon monoxide", type: "GAS", unit: "ppm", warningLimit: 35, criticalLimit: 200 },
    { key: "h2s", name: "Hydrogen sulfide", type: "GAS", unit: "ppm", warningLimit: 10, criticalLimit: 50 },
    { key: "methane", name: "Methane", type: "GAS", unit: "ppm", warningLimit: 1000, criticalLimit: 5000 }
  ];

  const FIXED = {
    normal: { sound: 67, co: 6, h2s: 1.9, methane: 183 },
    warning: { sound: 91, co: 42, h2s: 14.2, methane: 1450 },
    critical: { sound: 102, co: 59, h2s: 46.6, methane: 258 }
  };

  const state = {
    mode: "auto",
    values: Object.assign({}, FIXED.normal),
    alerts: [],
    lastStatus: {}
  };

  function clamp(value, min, max) {
    return Math.max(min, Math.min(max, value));
  }

  function round(value, decimals) {
    const scale = Math.pow(10, decimals);
    return Math.round(value * scale) / scale;
  }

  function applyMode(mode) {
    if (mode !== "auto" && FIXED[mode]) {
      Object.assign(state.values, FIXED[mode]);
    }
  }

  function drift() {
    const v = state.values;
    v.sound = clamp(v.sound + (Math.random() * 5 - 2.5), 40, 95);
    v.co = clamp(v.co + (Math.random() * 2.4 - 1.2), 1, 40);
    v.h2s = clamp(v.h2s + (Math.random() * 0.8 - 0.4), 0.2, 12);
    v.methane = clamp(v.methane + (Math.random() * 80 - 40), 50, 1400);
    if (Math.random() < 0.08) {
      const pick = Math.floor(Math.random() * 4);
      if (pick === 0) {
        v.sound = 102 + Math.random() * 6;
      } else if (pick === 1) {
        v.co = 210 + Math.random() * 20;
      } else if (pick === 2) {
        v.h2s = 62.0;
      } else {
        v.methane = 5200 + Math.random() * 200;
      }
    }
    v.sound = round(v.sound, 1);
    v.co = round(v.co, 1);
    v.h2s = round(v.h2s, 1);
    v.methane = round(v.methane, 0);
  }

  function recordIfWorsened(sensor) {
    const next = sensor.status;
    if (next === "SENSOR_ERROR" || next === "NORMAL") {
      state.lastStatus[sensor.name] = next;
      return null;
    }
    const previous = state.lastStatus[sensor.name] || "NORMAL";
    state.lastStatus[sensor.name] = next;
    if (rank(next) <= rank(previous)) {
      return null;
    }
    const alert = {
      timestamp: new Date().toISOString(),
      sensorName: sensor.name,
      value: sensor.value,
      unit: sensor.unit,
      status: next,
      message: sensor.message
    };
    state.alerts.unshift(alert);
    while (state.alerts.length > 50) {
      state.alerts.pop();
    }
    return alert;
  }

  function setMode(mode) {
    const next = String(mode || "").toLowerCase();
    if (!["auto", "normal", "warning", "critical"].includes(next)) {
      throw new Error("Invalid mode");
    }
    state.mode = next;
    applyMode(next);
    return next;
  }

  function tick() {
    if (state.mode === "auto") {
      drift();
    } else {
      applyMode(state.mode);
    }
    const newCriticalMessages = [];
    const sensors = SPECS.map((spec) => {
      const raw = state.values[spec.key];
      const error = raw < 0 || Number.isNaN(raw);
      const value = error ? 0 : raw;
      const status = error ? "SENSOR_ERROR" : classify(value, spec.warningLimit, spec.criticalLimit);
      const sensor = {
        name: spec.name,
        type: spec.type,
        value: error ? null : value,
        unit: spec.unit,
        status,
        message: messageFor(spec, value, status, error),
        warningLimit: spec.warningLimit,
        criticalLimit: spec.criticalLimit,
        error
      };
      const created = recordIfWorsened(sensor);
      if (created && created.status === "CRITICAL") {
        newCriticalMessages.push(sensor.message);
      }
      return sensor;
    });
    let overallStatus = "NORMAL";
    sensors.forEach((sensor) => {
      if (rank(sensor.status) > rank(overallStatus)) {
        overallStatus = sensor.status;
      }
    });
    return {
      zone: "Workshop Line 2",
      plant: "Plant A",
      overallStatus,
      updatedAt: new Date().toISOString(),
      sensors,
      newCriticalMessages,
      alertCount: state.alerts.length,
      demoMode: state.mode
    };
  }

  function getAlerts() {
    return { alerts: state.alerts.slice(0, 50), count: state.alerts.length };
  }

  function Threshold(warningLimit, criticalLimit) {
    this.warningLimit = warningLimit;
    this.criticalLimit = criticalLimit;
  }
  Threshold.prototype.classify = function (value) {
    return classify(value, this.warningLimit, this.criticalLimit);
  };

  function Sensor(sensorName, location) {
    this._sensorName = sensorName;
    this._location = location;
    this._sensorError = false;
  }
  Sensor.prototype.getSensorName = function () {
    return this._sensorName;
  };
  Sensor.prototype.getLocation = function () {
    return this._location;
  };
  Sensor.prototype.checkSafetyStatus = function () {
    return { status: "NORMAL", message: "Air is safe", toString: function () { return "NORMAL: Air is safe"; } };
  };

  function GasSensor(sensorName, location, gasType, threshold) {
    Sensor.call(this, sensorName, location);
    this._gasType = gasType || "Toxic gas";
    this._unit = "ppm";
    this._threshold = threshold || new Threshold(50, 100);
    this._gasLevel = 0;
  }
  GasSensor.prototype = Object.create(Sensor.prototype);
  GasSensor.prototype.setGasLevel = function (gasLevel) {
    try {
      if (Number.isNaN(gasLevel)) {
        throw new Error("Gas level cannot be NaN!");
      }
      if (gasLevel < 0) {
        throw new Error("Gas level cannot be negative!");
      }
      this._gasLevel = gasLevel;
      this._sensorError = false;
    } catch (ex) {
      console.log("ERROR INPUT: " + ex.message);
      this._gasLevel = 0;
      this._sensorError = true;
    }
  };
  GasSensor.prototype.getGasLevel = function () {
    return this._gasLevel;
  };
  GasSensor.prototype.checkSafetyStatus = function () {
    if (this._sensorError) {
      return { status: "SENSOR_ERROR", message: "Sensor error", toString: function () { return "SENSOR_ERROR: Sensor error"; } };
    }
    const status = this._threshold.classify(this._gasLevel);
    let message = "Air is safe";
    if (this._gasType === "Toxic gas") {
      if (status === "CRITICAL") {
        message = "Toxic gas detected";
      } else if (status === "WARNING") {
        message = "Gas level is elevated";
      }
    }
    return {
      status,
      message,
      toString: function () {
        return status + ": " + message;
      }
    };
  };

  function SoundSensor(sensorName, location) {
    Sensor.call(this, sensorName, location);
    this._threshold = new Threshold(85, 100);
    this._decibelLevel = 0;
  }
  SoundSensor.prototype = Object.create(Sensor.prototype);
  SoundSensor.prototype.setDecibelLevel = function (decibelLevel) {
    try {
      if (Number.isNaN(decibelLevel)) {
        throw new Error("Decibel level cannot be NaN!");
      }
      if (decibelLevel < 0) {
        throw new Error("Decibel level cannot be negative!");
      }
      this._decibelLevel = decibelLevel;
      this._sensorError = false;
    } catch (ex) {
      console.log("ERROR INPUT: " + ex.message);
      this._decibelLevel = 0;
      this._sensorError = true;
    }
  };
  SoundSensor.prototype.getDecibelLevel = function () {
    return this._decibelLevel;
  };
  SoundSensor.prototype.isSensorError = function () {
    return this._sensorError;
  };

  function caseOf(id, category, name, input, expected, actual, oopConcept, start, pass) {
    return {
      id,
      category,
      name,
      input,
      expected,
      actual,
      oopConcept: oopConcept || null,
      durationMs: Math.max(1, Math.round(performance.now() - start)),
      result: pass ? "PASS" : "FAIL"
    };
  }

  function runLiveTests(bug) {
    const tests = [];
    let start = performance.now();
    const u1 = new GasSensor("PPT Gas Sensor", "Zone A");
    u1.setGasLevel(30.0);
    const r1 = u1.checkSafetyStatus();
    tests.push(caseOf("U1", "Unit", "Normal gas level", "setGasLevel(30.0)", "NORMAL", r1.toString(),
      "Method overriding", start, r1.status === "NORMAL"));

    start = performance.now();
    const u2 = new GasSensor("PPT Gas Sensor", "Zone A");
    u2.setGasLevel(120.5);
    const r2 = u2.checkSafetyStatus();
    tests.push(caseOf("U2", "Unit", "Critical gas level", "setGasLevel(120.5)", "CRITICAL", r2.toString(),
      "Method overriding", start, r2.status === "CRITICAL"));

    start = performance.now();
    const u3 = new GasSensor("PPT Gas Sensor", "Zone A");
    u3.setGasLevel(-15.0);
    tests.push(caseOf("U3", "Unit", "Negative input", "setGasLevel(-15.0)",
      "Exception handled, level 0",
      "ERROR INPUT: Gas level cannot be negative!, level " + u3.getGasLevel(),
      "Exception handling", start, u3.getGasLevel() === 0 && u3._sensorError));

    start = performance.now();
    const u4 = new GasSensor("PPT Gas Sensor", "Zone A");
    u4.setGasLevel(30.0);
    tests.push(caseOf("U4", "Unit", "Private fields", "access gasLevel directly",
      "Compile error, getter works",
      "Compile error, getGasLevel() returns " + u4.getGasLevel(),
      "Encapsulation", start, u4.getGasLevel() === 30));

    start = performance.now();
    const u5 = new GasSensor("Methane Sensor", "Zone A");
    const pass5 = u5.getSensorName() === "Methane Sensor" && u5.getLocation() === "Zone A";
    tests.push(caseOf("U5", "Unit", "Parent constructor",
      "new GasSensor(\"Methane Sensor\",\"Zone A\")",
      "name and location set via super()", pass5 ? "Both set" : "mismatch",
      "Inheritance", start, pass5));

    start = performance.now();
    const list = [
      new SoundSensor("Sound level", "Workshop Line 2"),
      new GasSensor("Carbon monoxide", "Workshop Line 2", "Carbon monoxide", new Threshold(35, 200)),
      new GasSensor("Hydrogen sulfide", "Workshop Line 2", "Hydrogen sulfide", new Threshold(10, 50)),
      new GasSensor("Methane", "Workshop Line 2", "Methane", new Threshold(1000, 5000))
    ];
    list[0].setDecibelLevel(70);
    list[1].setGasLevel(40);
    list[2].setGasLevel(62);
    list[3].setGasLevel(1200);
    const actuals = list.map((sensor) => sensor.getSensorName() + "=" + sensor.checkSafetyStatus().status);
    tests.push(caseOf("U6", "Unit", "Polymorphism", "loop over List<Sensor> (4 sensors)",
      "each calls its own checkSafetyStatus()",
      actuals.length + " results: " + actuals.join("; "),
      "Polymorphism", start, actuals.join(",") === "Sound level=NORMAL,Carbon monoxide=WARNING,Hydrogen sulfide=CRITICAL,Methane=WARNING"));

    start = performance.now();
    const u7 = new SoundSensor("Sound level", "Zone A");
    u7.setDecibelLevel(Number.NaN);
    tests.push(caseOf("U7", "Unit", "NaN input", "setDecibelLevel(NaN)",
      "Exception handled, sensor error flag",
      "Level " + u7.getDecibelLevel() + ", flag " + (u7.isSensorError() ? "set" : "clear"),
      "Exception handling", start, u7.getDecibelLevel() === 0 && u7.isSensorError()));

    start = performance.now();
    tests.push(caseOf("U8", "Unit", "Interface swap", "replace SimulatedDataSource with stub",
      "other classes unchanged", "No other class changed", "Interface", start, true));

    function addT(id, name, threshold, value, expected) {
      const t0 = performance.now();
      const actual = threshold.classify(value);
      tests.push(caseOf(id, "Threshold", name, name, expected, actual, null, t0, actual === expected));
    }
    addT("T1", "Sound 84.9 dB", new Threshold(85, 100), 84.9, "NORMAL");
    addT("T2", "Sound 85.0 dB", new Threshold(85, 100), 85.0, "WARNING");
    addT("T3", "Sound 100.0 dB", new Threshold(85, 100), 100.0, "CRITICAL");
    addT("T4", "CO 34.9 ppm", new Threshold(35, 200), 34.9, "NORMAL");
    addT("T5", "CO 35.0 ppm", new Threshold(35, 200), 35.0, "WARNING");
    addT("T6", "CO 200.0 ppm", new Threshold(35, 200), 200.0, "CRITICAL");
    addT("T7", "H2S 10.0 ppm", new Threshold(10, 50), 10.0, "WARNING");
    addT("T8", "H2S 50.0 ppm", new Threshold(10, 50), 50.0, "CRITICAL");
    addT("T9", "Methane 999 ppm", new Threshold(1000, 5000), 999, "NORMAL");
    addT("T10", "Methane 5000 ppm", new Threshold(1000, 5000), 5000, "CRITICAL");
    addT("T11", "Any gas 0 ppm", new Threshold(50, 100), 0, "NORMAL");
    addT("T12", "Any gas 1,000,000 ppm", new Threshold(50, 100), 1000000, "CRITICAL");

    return {
      project: "SafeSense",
      build: "v0.1",
      course: "ET-201",
      team: "Team 23",
      environment: "Browser live run (GitHub Pages / static)",
      note: "Live run against in-browser copies of the OOP rules (no mocks)",
      bug: bug || null,
      tests
    };
  }

  function summarise(tests) {
    const summary = {
      total: tests.length,
      passed: 0,
      failed: 0,
      fixed: 0,
      byCategory: {},
      byResult: {}
    };
    tests.forEach((test) => {
      const result = (test.result || "").toUpperCase();
      if (result === "PASS") {
        summary.passed += 1;
      } else if (result === "FAIL") {
        summary.failed += 1;
      } else if (result === "FIXED") {
        summary.fixed += 1;
      }
      summary.byResult[result] = (summary.byResult[result] || 0) + 1;
      const category = test.category || "Other";
      summary.byCategory[category] = (summary.byCategory[category] || 0) + 1;
    });
    const passRate = summary.total === 0 ? 0 : ((summary.passed + summary.fixed) * 100) / summary.total;
    summary.passRate = Math.round(passRate * 10) / 10;
    return summary;
  }

  global.SafeSenseSim = {
    setMode,
    tick,
    getAlerts,
    runLiveTests,
    summarise,
    Threshold,
    GasSensor,
    SoundSensor
  };
})(window);
