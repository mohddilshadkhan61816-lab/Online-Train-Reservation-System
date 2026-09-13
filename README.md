# RailEase — Online Train Reservation System

A polished Java Swing + SQLite desktop reservation system. It provides secure JDBC login, ticket booking with generated PNRs, and safe cancellation after fetching the booking.

## Run

Requirements: Java 8+ and Maven 3.9+.

```powershell
mvn compile exec:java
```

The SQLite database is created automatically as `railease.db` in the project folder.

## Demo access

| Username | Password |
| --- | --- |
| `admin` | `admin123` |
| `traveler` | `welcome123` |

## Included trains

| Number | Train |
| --- | --- |
| 12002 | Bhopal Shatabdi |
| 12952 | Mumbai Rajdhani |
| 12301 | Howrah Rajdhani |
| 12627 | Karnataka Express |

All database queries use `PreparedStatement` parameters; the login and booking database contain starter records only for demonstration.
