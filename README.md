# AetherUtilities

A client-side utility mod for **Minecraft 26.3**, built with Fabric.

AetherUtilities brings together useful quality-of-life features designed to improve inventory management, gameplay convenience, visibility, and interaction with the Minecraft world.

## Features

### Auto Tool

Automatically selects the most suitable tool when attacking a block.

- Automatically selects the appropriate tool before breaking a block.
- Searches the hotbar first, from left to right.
- If no suitable tool is found, searches the main inventory.
- If the tool is in the main inventory:
    - Moves it to the first available empty hotbar slot.
    - If the hotbar is full, replaces the currently selected slot.
- Prioritizes swords when targeting bamboo blocks and bamboo variants.
- Automatically retries the initial attack when switching to a sword for bamboo.
- Can be enabled or disabled independently.

### Auto Refill

Automatically replaces depleted block stacks in the hotbar.

- Detects when a block stack is completely consumed.
- Searches for an identical replacement stack.
- Prioritizes other hotbar slots, searching from left to right.
- If no replacement is found in the hotbar, searches the main inventory.
- Transfers the replacement stack into the depleted slot.
- Matches both the item and its components.
- Can be enabled or disabled independently.

### Inventory Sorter

Organizes inventories and simplifies item transfers.

- Sorts items alphabetically by name.
- Supports ascending (A → Z) and descending (Z → A) sorting.
- Automatically stacks compatible item stacks before sorting.
- Provides quick inventory management buttons:
    - **Sort:** Sorts the available inventory.
    - **Store Existing:** Transfers items that already exist in the destination inventory.
    - **Store All:** Transfers all possible items to the destination inventory.
    - **Retrieve Existing:** Retrieves items that already exist in the player's inventory.
    - **Retrieve All:** Retrieves all possible items.
- Includes a configuration button for sorting preferences.

### Light Levels

Displays light level information around the player.

- Displays block light levels.
- Displays sky light levels.
- Three operating modes:
    - OFF
    - BLOCK LIGHT
    - SKY LIGHT
- Configurable scan radius.
- Updates automatically as the player moves.

### Zoom

Provides a smooth, configurable zoom feature.

- Hold the assigned key to zoom in.
- Configurable zoom strength from 2x to 6x.
- Automatically returns to normal view when the key is released.

### Gamma

Provides additional brightness control.

Three brightness modes:

- OFF — Normal brightness.
- x3 — Increased brightness.
- x15 — Maximum brightness.

Cycles through the available modes using a configurable key.

### Villager Trades

Displays additional locked villager trades.

- Shows upcoming locked trades in the villager trading interface.
- Allows players to preview future trades without unlocking them first.
- Keeps locked trade offers visually distinguishable from available trades.

---

## Configuration

AetherUtilities includes its own configuration screen.

Press **K** (default key) to open the AetherUtilities configuration menu.

Available settings:

| Feature | Configuration |
|---|---|
| Auto Tool | Enable / Disable |
| Auto Refill | Enable / Disable |
| Light Levels | Keybind and scan radius |
| Zoom | Keybind and zoom strength |
| Gamma | Keybind and brightness mode |

Inventory Sorter has its own settings button inside the inventory interface.

### Default Keybinds

| Action | Default Key |
|---|---|
| AetherUtilities Config | K |
| Light Levels | L |
| Zoom | C |
| Gamma | G |

Keybinds can be changed through Minecraft's Controls settings or the AetherUtilities configuration screen, where supported.

---

## Requirements

- Minecraft **26.3**
- Fabric Loader **0.19.5 or newer**
- Fabric API **0.161.0+26.3**
- Java **25 or newer**

## Installation

1. Install Fabric Loader for Minecraft 26.3.
2. Install the required Fabric API version.
3. Download the AetherUtilities `.jar` file.
4. Place the `.jar` file inside your Minecraft `mods` folder.
5. Launch Minecraft using the Fabric profile.

## Development

AetherUtilities is developed using:

- Java 25
- Minecraft 26.3
- Fabric Loader
- Fabric API
- Fabric Loom
- Gradle
- 
## License

This project is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for details.

### Building from Source

Clone the repository:

```bash
git clone https://github.com/natalietait05/AetherUtilities.git