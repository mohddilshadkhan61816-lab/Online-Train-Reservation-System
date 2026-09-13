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

## For run on local host open power shell and gave these command to it:

cd "C:\Users\User\OneDrive\Documents\ChatGPT\Online Train Reservation System"

& ".\.tools\apache-maven-3.9.16\bin\mvn.cmd" "-Dmaven.repo.local=.m2\repository" compile exec:java

                                          "OR"
                                       
## To use the shorter mvn command for this PowerShell session (It lasts only until you close that PowerShell window):

cd "C:\Users\User\OneDrive\Documents\ChatGPT\Online Train Reservation System"

$env:Path = "$PWD\.tools\apache-maven-3.9.16\bin;$env:Path"

mvn "-Dmaven.repo.local=.m2\repository" compile exec:java
                                
