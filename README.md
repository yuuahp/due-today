# Due Today
📅 Simple subscription management on your calendar. Self-hostable. No vendor lock-in.

## Features
- Track your subscription on your favorite calendar app (Google Calendar, Apple Calendar, Outlook, etc.)
- Self-hostable with Docker
- Automatic currency conversion with [Frankfurter API](https://frankfurter.dev/)

## Run with Docker
Clone this repository and run `docker compose up -d` in the project directory.

## Run locally
Tested on Java 25.

1. Clone this repository.
2. Run `./gradlew build` in macOS/Linux or `gradlew.bat build` in Windows.
3. Run `java -jar build/libs/due-today-1.0.0-all.jar` to start the application.

## Server Configuration
Place `config.toml` in the project root. Example:
```toml
# Optional. The port to listen. Default is 8080.
port = 1234

# Optional. Configure automatic currency conversion. Omit this table to disable currency conversion.
[exchange-rate]
to = "JPY" # The currency to convert to.
host = "api.frankfurter.dev" # The host of the exchange rate API. Default is "api.frankfurter.dev".

# Optional. Configure the event title formats. 
[strings]
trial-start = "sub: Trial for {service} start"
trial-end = "due({price}): Trial for {service} end"
due-date = "due({price}): {service} due today" # e.g. due(JPY 450.00): iCloud 200GB due today
```

## Subscription Configuration
Place `subscriptions.toml` in the project root. Example:
```toml
[icloud-200gb] # This will be used as the event ID, so it must be unique.
service = "iCloud 200GB" # The name of the subscription service. This will be used in the event title.
price.currency = "JPY" # The billed currency.
price.amount = 450.00 # The billed amount.
start-date = 2026-02-14 # The start date of the subscription.
end-date = 2026-08-14 # Optional. The end date of the subscription. If omitted, the subscription will be considered active indefinitely.
interval.type = "monthly" # The billing interval. Can be "daily", "weekly", "monthly", or "annually".
interval.value = 1 # Optional. The billing interval value. 1 by default. For example, interval.type = "monthly" and interval.value = 3 means the subscription is billed every 3 months.
trial.end-date = 2026-03-14 # Optional. The end date of the trial period. The date is exclusive, meaning that you will be charged on this date.
```