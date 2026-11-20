# Java Project Specifications
## Repository Information
- Build/management tool: Maven
## Repository Structure
SmartCardPlugin/
- pom.xml
- src/main/
	- java/me/ksmc/smartcardplugin/
		- Main.java (plugin startup and shutdown logic)
		- command/
			- SmartcardCommand.java (command manager and dispatcher)
			- TabCompleter.java (handle command tab completion suggestions - UNIMPLEMENTED)
		- listener/
			- SignInteractListener.java (fare devices and self-service machines activation dispatcher)
			- MenuListener.java (self-service machine menu navigation, UNIMPLEMENTED)
			- WideGateIronTrapdoorListener.java (UNIMPLEMENTED)
		- manager/
			- device/
				- type/
					- GateManager.java
					- TerminalManager.java 
				- EntryDeviceManager.java
				- ExitDeviceManager.java
				- PayDeviceManager.java
				- StaffDeviceManager.java
			- ticketmachine/ (UNIMPLEMENTED)
				- MainMenu.java
				- BuyTicketSubmenu.java
				- BuyPassSubmenu.java
				- SmartcardAddValueSubmenu.java
			- ItemManager.java (issue and confiscate player inventory items, retrieve and update fare media attributes)
			- EconomyManager.java (get and modify player balance and agency revenue)
		- util/
			- ItemUtils.java
			- StringUtils.java
			- FareRetriever.java
			- DeviceUtils.java
		- database/
			- FareMediaDatabase.java
		- dependency/
			- VaultAPI.java
	- resources/
		- plugin.yml
		- config.yml
		- fare_charts/
			- mtr_lrt_sample.csv
			- mtr_sample.csv


# Data Specifications
## Database
Need 1 SQLite database with 3 tables:
- 1 table to store data for smart cards
- 1 table to store data for passes
- 1 table to store company agencies and revenue information

## Data Storage Information
### Smart Card
Need database, each smart card will store:
- cardID: string
- agencyID: string
- balance: float
- expiryFromLastUse: string
- entryRecord: string (agencyID,stationCode,timestamp,accumulatedPenaltyFare)
- exitRecord: string (agencyID,stationCode,timestamp)
- last 5 transaction records, including the costs: string list (timestamp,agencyID,cost)
- the timestamp of the latest transaction, initially it will be the timestamp of the card issue (timestamp)

Database table and example values:

| card_id: INTEGER | agency_id: string | balance: float | expiry_from_last_use: string | entry_record: string (agencyID:stationCode:timestamp,accumulatedPenaltyFare) | exit_record: string (agencyID:stationCode:timestamp) | transactions: string list (timestamp:agencyID:cost)                                                                      | last_used: timestamp |
| ---------------- | ----------------- | -------------- | ---------------------------- | ---------------------------------------------------------------------------- | ---------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------ | -------------------- |
| 1                | MTR               | 135.7          | 365d                         | MTR,ADM,1789528864,60.8                                                      | MTR,TSY,1789530064                                   | 1786850464,MTR,7.4;<br>1786764064,MTR_LRT,5.5;<br>1786677664,MTR,13.7;<br>1786591264,MTR,60.8;<br>1786504864,MTR_LRT,8.0 | 1786850464           |
| 2                | MTR               | -6.2           |                              |                                                                              |                                                      | 1786850464,MTR,7.4;<br>1786764064,MTR_LRT,5.5;<br>1786677664,MTR,13.7;<br>1786591264,MTR,60.8;<br>1786504864,MTR_LRT,8.0 |                      |
SQL create table statement:

CREATE TABLE IF NOT EXISTS SmartCards (
    card_id INTEGER PRIMARY KEY,
    agency_id VARCHAR(30) NOT NULL,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
    expiry_from_last_use VARCHAR(20),
    entry_record VARCHAR(60),
    exit_record VARCHAR(60),
    transactions TEXT,
	last_used BIGINT
);

Note: SQLite will automatically assign each row with a unique INTEGER card_id


### Single-journey Ticket
No database, each single-journey ticket will store:
- agencyID: string
- origin: string
- destination: string
- hasEnteredGate: boolean
- expiry: timestamp (default 24 hours after ticket issued)

### Exit-only Ticket
No database, each exit-only ticket will store:
- agencyID: string
- expiry: timestamp (default 5 minutes after issued)

### Pass
Need database, each pass will store:
- passID: string
- agencyID: string
- passType: int
- tripsRemaining: int (-1 for unlimited)
- hasEnteredGate: boolean (using int in database: 0 for false, 1 for true)
- expiry: timestamp

Database table and example values:

| pass_id: INTEGER | agency_id: string | pass_type: int | trips_remaining: int | has_entered_gate: int | expiry: timestamp |
| ---------------- | ----------------- | -------------- | -------------------- | --------------------- | ----------------- |
| 1                | STM               | 1              | 9                    | 0                     | 1789528864        |
| 2                | MTR               | 1              | -1                   | 1                     | 1789528864        |
SQL create table statement:

CREATE TABLE IF NOT EXISTS Passes (
    pass_id INTEGER PRIMARY KEY,
    agency_id VARCHAR(30) NOT NULL,
    pass_type INT NOT NULL,
    trips_remaining INT NOT NULL DEFAULT -1,
    has_entered_gate INT NOT NULL DEFAULT 0,
    expiry BIGINT
);

Note: SQLite will automatically assign each row with a unique INTEGER pass_id


### Company Revenue
Need database, each pass will store:
- agencyID: string
- revenue: float

Database table and example values:

| agency_id: string | revenue: float |
| ----------------- | -------------- |
| MTR               | 4698.67        |
| STM               | 0.0            |

SQL create table statement:

CREATE TABLE IF NOT EXISTS CompanyRevenue (
	agency_id VARCHAR(30) PRIMARY KEY,
	revenue DECIMAL(10, 2) NOT NULL DEFAULT 0.0
);


# GUI Specifications
## Ticket/Self-Service Machine

### Main Menu (3x9 slots - use only the 2nd row):

Menu title: “companyName Self-Service Machine”

| Slot number (left to right)                          | 1   | 2          | 3          | 4                                    | 5   | 6                   | 7                               | 8                                                 | 9   |
| ---------------------------------------------------- | --- | ---------- | ---------- | ------------------------------------ | --- | ------------------- | ------------------------------- | ------------------------------------------------- | --- |
| Item to represent                                    |     | Paper      | Book       | Nametag                              |     | Gold ingot          | Compass                         | Barrier block                                     |     |
| Description - title (when hover over the item)       |     | Buy Ticket | Buy a Pass | Buy “cardBrandName”                  |     | Smartcard Add-Value | Fare Enquiry                    | Stuck in Paid Area                                |     |
| Description - other lines (when hover over the item) |     |            |            | Non-refundable cost: $“purchaseCost” |     |                     | Check card/ticket/pass details. | Get an exit-only ticket.<br>Cost: $“purchaseCost” |     |
| Has a submenu?                                       |     | Yes        | Yes        | No                                   |     | Yes                 | No                              | No                                                |     |
Notes:
- for the purchaseCosts, they will be displayed with the currency exchange rate already calculated

### Submenus
- At any time, should a player make a mistake, they can press the Esc key to quit a submenu and no transaction will be made. However they will need to start all over again from the main menu.

#### Buy Ticket Submenu (3x9 slots - use all rows):
- unlimited station/zone options
- display a maximum of 8 stations/zones per page
- can have more than one page, navigate with buttons on menu

Submenu title: "Select Origin Station" / "Select Destination Station"
- If the self-service machine sign specifies a station, it signifies the origin station, in which case only the "Select Destination Station" submenu will display.
- Otherwise, "Select Origin Station" submenu will display first, and after the player has selected the origin station, "Select Destination Station" submenu will then display.

|     | 1   | 2             | 3   | 4         | 5   | 6         | 7   | 8         | 9   |
| --- | --- | ------------- | --- | --------- | --- | --------- | --- | --------- | --- |
| 1   |     | station 1     |     | station 2 |     | station 3 |     | station 4 |     |
| 2   |     | station 5     |     | station 6 |     | station 7 |     | station 8 |     |
| 3   |     | Previous page |     |           |     |           |     | Next page |     |


#### Buy a Pass Submenu (3x9 slots - use all rows):
- unlimited passType options
- display a maximum of 8 passTypes per page
- can have more than one page, navigate with buttons on menu

Submenu title: "Select a Pass"

|     | 1   | 2             | 3   | 4          | 5   | 6          | 7   | 8          | 9   |
| --- | --- | ------------- | --- | ---------- | --- | ---------- | --- | ---------- | --- |
| 1   |     | passType 1    |     | passType 2 |     | passType 3 |     | passType 4 |     |
| 2   |     | passType 5    |     | passType 6 |     | passType 7 |     | passType 8 |     |
| 3   |     | Previous page |     |            |     |            |     | Next page  |     |


#### Smartcard Add-Value Submenu (3x9 slots - use all rows):
- maximum 12 options for add-value amounts

Submenu title: "Add-Value Options"

|     | 1   | 2               | 3   | 4                | 5   | 6                | 7   | 8                | 9   |
| --- | --- | --------------- | --- | ---------------- | --- | ---------------- | --- | ---------------- | --- |
| 1   |     | Amount option 1 |     | Amount option 2  |     | Amount option 3  |     | Amount option 4  |     |
| 2   |     | Amount option 5 |     | Amount option 6  |     | Amount option 7  |     | Amount option 8  |     |
| 3   |     | Amount option 9 |     | Amount option 10 |     | Amount option 11 |     | Amount option 12 |     |
