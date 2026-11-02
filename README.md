# Porphyrome

A NeoForge Minecraft mod that adds a **Roman-inspired city-building and settlement system** — place a Forum to found a city, construct buildings with a hammer, and manage ownership, borders, and annexation through a live in-game map UI.

> **In Progress** — the city ownership, construction, networking, and Forum UI systems are functional. The Palace tier and the multi-tier production network are scaffolded but not yet complete.

---

## What it does

- **Found cities** by placing a **Forum** block. Each city tracks a name, owner, colour, tier, and a square territorial radius.
- **Construct buildings** (Manor, Hippodrome, Cathedral, Palace, Farm) by charging and swinging the **Hammer** item; each swing advances the block through 4 construction stages.
- **Annex and transfer** buildings between cities through a live, interactive **Forum map UI**, with per-city "borders open / closed" diplomacy settings.
- **Live multiplayer sync** — every placement, removal, ownership transfer, colour change, and border-policy change is broadcast to all players and reflected instantly in any open GUI.
- **Persistent city data** — cities and their building rosters are saved to the world via a `SavedData` manager and survive reloads.

---

## Feature highlights

### City & territory system
- `City` tracks name, owner UUID, colour, tier, radius, annexation policy, and a `Set<BlockPos>` of owned buildings.
- `CityManager` (a `SavedData`) is the single source of truth: found cities, annex pre-existing buildings in radius, transfer buildings on city collapse, and resolve `getCityAt` / `getCityOwningBuilding`.
- `CityBorderTracker` listens to `PlayerTickEvent` and sends an action-bar message when a player crosses into a new city (throttled by a horizontal-position cache).

### Construction
- `ConstructableBlock` is the base for all player-built structures — an `EntityBlock` with a `CONSTRUCTION_PROGRESS` (0–3) `IntegerProperty`, horizontal `FACING`, and a custom `VoxelShape`.
- `HammerItem` is a chargeable tool (bow-style `UseAnim`): holding it shows a live charge percentage in the action bar; releasing at full charge builds all `ConstructableBlock`s within a 3×3×3 radius, with particles, sound, and cooldown.

### Forum UI (the centrepiece)
- `ForumScreen` renders a **pan/zoom, player-relative rotating map** of every building in and around the city.
- A custom **watertight border-outline algorithm** draws per-city territory outlines: it checks the four cardinal neighbours of every grid cell, draws only the exposed edges, and fills inner corner notches so borders never overlap or leave gaps.
- Foreign buildings are ghost-overlaid and tinted; clicking one opens a context pop-up offering **Annex** (if in range and the owner allows it) or a **Closed** / **Too Far** message.
- Owners can rename their city, cycle through a palette of imperial colours, and toggle **Borders: OPEN / CLOSED** — all synced live.

### Workstation crafting UI
- `WorkstationRecipe` is a custom `Recipe<RecipeInput>` with **sized ingredients** (`SizedIngredient`), registered with its own serializer, type, and stream codec.
- `WorkstationScreen` provides a **searchable, paginated recipe book** (4×6 per page) with keyboard paging (arrows, Home/End, PageUp/PageDown), a custom `EditBox` search bar, and ingredient availability indicators that flip green/red based on the player's inventory.
- Creative-mode **middle-click cloning** of recipes, outputs, and ingredients via a dedicated payload.

### Networking
- ~15 typed payloads using NeoForge's modern `CustomPacketPayload` + `StreamCodec` pattern (no manual ` FriendlyByteBuf` byte-pushing).
- Server→client `Sync*` messages push live updates (building placed/removed, owner transfer, city style, annex permission) directly into any open `ForumScreen`.
- Client→server messages are **validated server-side**: annexation re-checks radius and diplomacy policy, style updates verify the forum position, creative cloning verifies `isCreative()`.

### Farm & network node scaffold
- `FarmBlock` / `FarmBlockEntity` demonstrate a crop-type system driven by an `EnumProperty<CropType>` and an `Item → CropType` mapping.
- `INetworkNode` is a block capability (tier, parent, child-update propagation) intended to model a Farm → House → Town production hierarchy; it is wired for farms and ready to extend.

---

## Architecture

| Package | Responsibility |
|---|---|
| `rome` | `RomeMod` — `@Mod` entry point; wires registries, client events, networking, and the border tracker |
| `rome.block` | `ConstructableBlock` base + Forum, Manor, Palace, Cathedral, Hippodrome, Farm, Workstation |
| `rome.blockentity` | Block entities; `WorkstationBlockEntity` is a `MenuProvider`, `FarmBlockEntity` implements `INetworkNode` + NBT persistence |
| `rome.city` | `City`, `CityManager` (`SavedData`), `CityData` (record + `StreamCodec`), `CityBorderTracker` |
| `rome.capability` | `INetworkNode` block capability and `CropType` enum |
| `rome.gui` | `BaseMenu`, `CustomEditBox`, and the `forum` / `workstation` / `palace` screen packages |
| `rome.network` | All `CustomPacketPayload` records and their handlers |
| `rome.recipe` | `WorkstationRecipe`, `RecipeResultSlot`, `InventoryCraftingUtil` |
| `rome.registry` | Deferred registers for blocks, items, block entities, menus, recipes, capabilities, creative tab, client events |
| `rome.util` | `Constant`, `Localisation` (plural-rule-aware i18n helper) |

---

## Technical highlights

- **Axis- and radius-based world scanning** using `BlockPos.betweenClosed` with clamped Y bounds for safe, bounded iteration.
- **Watertight territory rendering** — neighbour-difference edge detection plus corner-notch patching (see `ForumScreen.drawMapView`).
- **Player-relative map rotation** — building positions are rotated by the player's facing direction (N/E/S/W) before projection, so the map always reads "up = where you're looking."
- **Live GUI mutation via packets** — screens expose `addBuildingLive`, `removeBuildingLive`, `updateBuildingOwnerLive`, `updateCityStyleLive`, etc., so open menus mutate without reopening.
- **Defensive server-side validation** on every client→server payload (range, ownership, diplomacy policy, creative mode).
- **Plural localisation** — `Localisation` selects plural forms by language code (including Polish 2–4 / 5+ / teen rules) and sends action-bar feedback with the correct key.
- **Modern NeoForge patterns** — `DeferredRegister`, `DeferredBlock`/`DeferredItem`, `MapCodec`/`StreamCodec`, `SavedData.Factory`, `IMenuTypeExtension`, `BlockCapability`.

---

## Acknowledgements

Built as a personal project exploring NeoForge's block, capability, menu, recipe, and networking APIs. Minecraft is a trademark of Mojang Studios; this is an unofficial fan project and is not affiliated with or endorsed by Mojang or NeoForge.