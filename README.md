# SafeSense

University prototype (ET-201, Team 23) for a workplace gas and sound monitoring dashboard. Sensor values are simulated. The dashboard is the product; login and extra pages stay small.

## How to run

Requires **Java 17** and **Maven**.

```bash
cd SafeSense
mvn test
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). You will be sent to `/login`.

### Demo login

| Field | Value |
| --- | --- |
| Email | `aisha@demo.com` |
| Password | `Demo@123` |
| Name | Aisha Khan |
| Role | Supervisor |

The password is stored as a **BCrypt** hash (starts with `$2a$`). There is no registration page in this version (see the TODO on `User`).

Useful URLs after login:

- `/dashboard` — live cards, banner, chart, alerts (polls `GET /api/readings` every 2 seconds)
- `/tests` — sample test report and live OOP runner
- `/live`, `/alerts`, `/history`, `/limits`, `/settings` — “Coming soon”

PPT console demo (U1–U3):

```bash
mvn -q exec:java -Dexec.mainClass=com.safesense.PptDemo
```

Or run `SensorTest` / `PptDemo.main`.

## OOP concept map (for the presentation)

| Concept | Where it appears |
| --- | --- |
| Encapsulation | Private fields + getters/validated setters in `model/Sensor`, `GasSensor`, `SoundSensor`, `Threshold`, `Alert`, `Reading`, `User`, `TestCase`, `TestReport` |
| Inheritance | `GasSensor` and `SoundSensor` extend `Sensor` (`super(name, location)`) |
| Method overriding | `Sensor.checkSafetyStatus()`; `@Override` in `GasSensor` and `SoundSensor` returning `StatusResult` |
| Object creation | `SensorManager.createZoneSensors()` — `new SoundSensor(...)`, three `new GasSensor(...)` |
| Polymorphism | `List<Sensor>` loop in `SensorManager.refresh()` calling `checkSafetyStatus()` |
| Exception handling | `setGasLevel` / `setDecibelLevel` catch `IllegalArgumentException`, print `ERROR INPUT:`, reset to 0, set sensor-error flag; `AuthenticationException`; `GlobalExceptionHandler` |
| Interface | `datasource/SensorDataSource`; `SimulatedDataSource` today, `HardwareDataSource` stub for later |

PPT numeric checks live in `src/test/java/com/safesense/SensorTest.java`:

- `setGasLevel(30.0)` → NORMAL  
- `setGasLevel(120.5)` → CRITICAL  
- `setGasLevel(-15.0)` → handled, level 0  

Default limits (warning / critical): sound 85 / 100 dB, CO 35 / 200 ppm, H2S 10 / 50 ppm, methane 1000 / 5000 ppm. The PPT generic gas sensor uses 50 / 100 ppm.

## Test results page

Open **Test results** in the sidebar or go to [http://localhost:8080/tests](http://localhost:8080/tests) (login required).

- **Sample report** (`GET /api/test-results`) is the demonstration JSON in `src/main/resources/data/test-results.json` (36 cases: 35 PASS, 1 FIXED, pass rate 100% after the T8 fix). Totals are counted from the file, not hard-coded.
- **Live run** (`POST /api/test-results/run`) executes real checks on the Java classes (U1–U8, T1–T12, L6). After the `>=` threshold fix, T8 is **PASS** on a live run (the sample report keeps T8 as **FIXED** to show BUG-01).

### Short demo script

1. Open `/tests` and show the four summary cards plus the stacked bar.  
2. Filter **Threshold**, open **T8**, and walk through the BUG-01 card (symptom, root cause, fix, retest).  
3. Click **Run live tests** and show real `actual` values and durations from the JVM.  
4. Open **OOP coverage** and map each concept to the test IDs, then to the classes in the table above.

## Semester-4 communication system

`SensorManager` only depends on the `SensorDataSource` interface (`List<Reading> readAll()`). Today Spring injects `SimulatedDataSource`.

To connect real hardware later:

1. Implement `readAll()` in `HardwareDataSource` (MQTT client, serial adapter, or REST poller).  
2. Register that class as the Spring bean for `SensorDataSource` (or swap the `@Component` on the simulator).  
3. Leave `SensorManager`, `AlertService`, `ZoneStatusService`, REST controllers, and the dashboard unchanged.

The GUI already consumes `GET /api/readings` and `GET /api/alerts`. It does not know whether values came from the simulator or a plant gateway.

## REST API

| Method | Path | Notes |
| --- | --- | --- |
| POST | `/api/login` | Session cookie |
| POST | `/api/logout` | |
| GET | `/api/me` | Current user |
| GET | `/api/readings` | Zone snapshot |
| GET | `/api/alerts` | Latest 50 |
| POST | `/api/demo-mode?mode=auto\|normal\|warning\|critical` | |
| GET | `/api/test-results` | Sample report + summary |
| POST | `/api/test-results/run` | Live OOP tests + summary |

Unauthenticated API calls return **401** JSON. Unknown demo modes return **400** `{ "error": "Invalid mode" }`.
