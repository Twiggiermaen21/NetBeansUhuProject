# Project Documentation: Messi GOAT GYM

Detailed technical reference for the gym management system, covering Data Access Objects (DAO), Views (Swing GUI), and Entity Models.

---

## 🗄️ Data Access Layer (DAO)
*Classes responsible for direct interaction with the database using Hibernate Session.*

### `ActivityDAO.java`
| Method Signature | Precise Functionality |
| :--- | :--- |
| `insertActivity(Session session, Activity activity)` | Persists a transient `Activity` instance into the database using `session.persist()`. This makes the object managed and schedules an SQL INSERT. |
| `updateActivity(Session session, Activity activity)` | Merges the state of the given detached `Activity` instance into the current persistence context using `session.merge()`. Schedules an SQL UPDATE. |
| `deleteActivityById(Session session, String activityId)` | Retrieves an entity reference by ID and removes it. Returns `true` if the entity existed and was removed; `false` if the ID was not found. |
| `isTrainerOccupied(...)` | Executes an HQL COUNT query to check if a specific trainer is already assigned to an activity on the given day and hour. **Logic:** If `currentActivityId` is provided, it excludes that ID from the check to allow editing an existing record without self-conflict. |
| `existAId(Session session, String aId)` | Verifies existence of a primary key by attempting to fetch a single result via HQL. Returns `true` if found, handles `NoResultException` by returning `false`. |
| `getMaxActivityCode(Session session)` | Executes a query sorting Activity IDs in descending order and limits results to 1. Used to retrieve the current highest ID (e.g., "AC10") to algorithmically generate the next ID. |
| `findActivityById(Session session, String activityId)` | Wrapper for `session.find()`. Returns the fully initialized `Activity` object or `null` if not found. |
| `findAllActivities(Session session)` | Executes a generic HQL query `SELECT a FROM Activity a` to return a List of all registered activities. |
| `getActivityStatisticsById(Session session, String aId)` | Performs a `LEFT JOIN` between Activity and Client sets. Returns an `Object[]` containing: `[0]` Activity Name, `[1]` Count of associated clients. |

### `ClientDAO.java`
| Method Signature | Precise Functionality |
| :--- | :--- |
| `existMemberNumber(Session session, String memberNum)` | Checks if a client with the specific Member Number exists using HQL. Returns boolean. |
| `existDNI(Session session, String dni)` | Checks if a client with the specific Personal ID (DNI/PESEL) exists. Used for validation before insertion to prevent logical duplicates. |
| `insertClient` / `updateClient` | Standard Hibernate persistence methods (persist/merge) for the Client entity. |
| `returnClientByMemberNumber(...)` | Fetches a single `Client` entity based on the unique member number. Returns `null` if `NoResultException` occurs. |
| `getAllClients(Session session)` | Retrieves all rows from the CLIENT table mapped to the Client class. |
| `deleteClient(Session session, Client client)` | Removes a persistent client instance from the database. |
| `getClientsByCategory(Session session, char category)` | Executes a projection query selecting only names (`mName`) and categories (`mcategoryMember`) for clients matching the provided category char. Returns a List of `Object[]`. |
| `getNameAndPhone(Session session)` | Projection query returning only Name and Phone Number for all clients. Useful for contact lists/views. |
| `getClientByName(Session session, String name)` | Finds a client by exact name match. Note: may throw exception if multiple clients have the exact same name (uses `uniqueResult`). |
| `getMaxMemberNumber(Session session)` | Returns the string value of the highest `mNum` currently in the database. Used for ID auto-generation. |
| `getGlobalStatistics(Session session)` | Aggregates complex data in one call. Returns `Object[]`: <br> `[0]` Total count of clients (Long)<br> `[1]` List of all birthdates (String)<br> `[2]` List of [Category, Price] pairs. |
| `getStatisticsForActivity(Session session, String aId)` | Similar to global stats, but filtered by Activity ID via JOIN. Returns total participants, their birthdates, and revenue data for that specific activity. |

### `TrainerDAO.java`
| Method Signature | Precise Functionality |
| :--- | :--- |
| `insertTrainer` / `updateTrainer` | Persists new Trainer entities or updates existing ones in the database. |
| `deleteTrainerById(Session session, String trainerId)` | Finds a Trainer by PK (tCod) and removes it if it exists. Returns success status boolean. |
| `existTrainerID(Session session, String id)` | Checks if a Trainer with a specific government ID/DNI (`tidNumber`) already exists to prevent duplicate personal records. |
| `getTrainerByCod(Session session, String trainerCod)` | Retrieves the Trainer entity by its primary key (Code). |
| `getMaxTrainerCode(Session session)` | Fetches the highest alphanumeric Trainer Code (e.g., "T099") to assist in generating the next sequential code. |
| `findAllTrainers(Session session)` | Returns a complete list of Trainer entities. Contains error handling to return an empty list instead of crashing on failure. |

---

## 🖥️ View Layer (GUI)
*Swing-based classes responsible for the user interface and presentation logic.*

### `MainWindow.java`
*The core application window containing the main table, navigation menu, and action buttons.*

| Method Signature | Precise Functionality |
| :--- | :--- |
| `setViewName(String name)` | Updates the central label (`viewNameLabel`) to indicate the current module (e.g., "Clients", "Trainers"). |
| `setTableData(String[] columnNames, Object[][] data)` | Initializes the `JTable` model. **Logic:** <br> 1. Creates a non-editable DefaultTableModel.<br> 2. Detects if column 0 contains `ImageIcon`. If yes, adapts row height/width for images.<br> 3. If no images, sets standard layout. |
| `setupTableSorter()` | Initializes `TableRowSorter` for the table and populates the search `JComboBox` with current column names. |
| `filterTable()` | Applies a regex filter to the table sorter based on the text in `jSearchText` and the selected column in `jSearchBox`. Supports case-insensitive searching. |
| `autoResizeColumns()` | Iterates through every column and row to calculate the maximum preferred width of content and resizes columns accordingly. |
| `getSelectedRow()` / `getSelectedValueAt(...)` | Returns the index or value of the selected row. Converts **View Index** (sorted/filtered) to **Model Index** to ensure the correct record is processed. |
| `setButtonLabels(...)` | Dynamically renames the CRUD buttons (e.g., "Add Client" vs "Add Trainer") depending on the active view context. |
| `initSearchListeners()` | Adds `DocumentListener` to the search field to trigger real-time filtering as the user types. |

### `DataUpdateWindow.java`
*A polymorphic form used for creating and editing Clients, Trainers, and Activities.*

| Method Signature | Precise Functionality |
| :--- | :--- |
| `Accessors (getKod, setKod, etc.)` | Standard getters/setters to interact with `JTextField` components. |
| `getSelectedDate()` / `setSelectedDate(...)` | Interacts with the `JDateChooser` component (e.g., Join Date, Activity Date). |
| `getBirthdayDate()` / `setBirthdayDate(...)` | Interacts with the secondary `JDateChooser` specifically for the Client's birthday. |
| `setFieldLabels(...)` | Accepts strings to rename `JLabel` components. Allows the window to morph between "Client Form" and "Activity Form". |
| `setFieldVisible(Object field, boolean visible)` | Toggles visibility of UI components to hide irrelevant fields based on context. |

### `CalculateWindow.java` & `ConnectionView.java`
| Method Signature | Precise Functionality |
| :--- | :--- |
| **CalculateWindow**: `setResults(...)` | Populates the GUI text fields with pre-calculated statistical data. Sets fields to non-editable. |
| **ConnectionView**: `getUsername()` / `getPassword()` | Extracts raw text from input fields. |
| **ConnectionView**: `addConnectListener(...)` | Registers the controller's logic to the "Login" button. |

---

## 📦 Model Layer (Entities)
*POJO classes annotated with JPA/Hibernate metadata.*

### `Activity.java`
* **Fields:** `aId` (PK), `aName`, `aDescription`, `aPrice`, `aDay`, `aHour`.
* **Relationships:**
    * `@ManyToMany` with `Client` (via `PERFORMS` table).
    * `@ManyToOne` with `Trainer` (mapped by `atrainerInCharge`).
* **Key Method:** `getDisplayName()` - Returns `aName` for UI display purposes (e.g., in ComboBoxes).

### `Client.java`
* **Fields:** `mNum` (PK), `mName`, `mId` (Gov ID), `mBirthdate`, `mPhone`, `memailMember`, `mstartingDateMember`, `mcategoryMember`.
* **Relationships:** `@ManyToMany` with `Activity` (inverse side).
* **Key Feature:** `NamedQueries` - Pre-compiled HQL queries for efficient searching.

### `Trainer.java`
* **Fields:** `tCod` (PK), `tName`, `tidNumber`, `tphoneNumber`, `tEmail`, `tDate`, `tNick`.
* **Relationships:** `@OneToMany` with `Activity`.
* **Key Method:** `toString()` - Overridden to return `tName`. This is critical for Swing `JComboBox` components to correctly display the Trainer's name.

---
&copy; 2026 Messi GOAT GYM System Documentation.
