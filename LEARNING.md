# Learning Minecraft Modding with Clojure

## What We Have Now

A **minimal working Fabric mod** that:
- ✅ Compiles successfully
- ✅ Registers a simple tennis ball item
- ✅ Uses Clojure for all logic

## Project Structure

```
src/main/
├── clojure/tennisball/
│   ├── core.clj       # Main mod logic - item registration
│   └── entity.clj     # Placeholder for future entity code
├── java/com/tennisball/
│   └── TennisBallMod.java  # Entry point that calls Clojure
└── resources/
    ├── fabric.mod.json     # Mod metadata
    └── assets/tennisball/  # Textures, models, lang files
```

## How It Works

### 1. Mod Entry Point (Java)
[TennisBallMod.java](src/main/java/com/tennisball/TennisBallMod.java) is the entry point. Fabric calls it when loading the mod. It simply calls our Clojure code:

```java
public void onInitialize() {
    IFn init = Clojure.var("tennisball.core", "init");
    init.invoke();
}
```

### 2. Core Logic (Clojure)
[core.clj](src/main/clojure/tennisball/core.clj) registers our item:

```clojure
(defn register-item []
  (Registry/register 
    Registries/ITEM
    (Identifier/of "tennisball" "tennis_ball")
    (Item. (Item$Settings.))))
```

### 3. Resources
- **Texture**: `assets/tennisball/textures/item/tennis_ball.png`
- **Model**: `assets/tennisball/models/item/tennis_ball.json` (references the texture)
- **Language**: `assets/tennisball/lang/en_us.json` (item name)

## Testing the Mod

### Run in Development
```bash
gradle runClient
```

This launches Minecraft with your mod loaded. Look for "Tennis Ball Mod" in the logs.

### Find Your Item
In creative mode, use `/give @p tennisball:tennis_ball` to get the item.

## Next Steps to Learn

### Step 1: See Your Item in Game
Currently the item exists but isn't in any creative tab. Add it manually with the command above.

### Step 2: Add Custom Behavior
Make the item do something when right-clicked by implementing a custom Item class in Clojure.

### Step 3: Create the Entity
Add a throwable projectile entity that spawns when you use the item.

### Step 4: Add Bouncing Physics
Make the projectile bounce off surfaces with custom collision handling.

## Key Minecraft Concepts

### Registries
Everything in Minecraft (items, blocks, entities) must be **registered** with a unique identifier:
- Format: `modid:item_name`
- Example: `tennisball:tennis_ball`

### Items vs Entities
- **Item**: Something in your inventory
- **Entity**: Something in the world (thrown ball, mob, etc.)

### Client vs Server
- `world.isClient()` - runs only on the player's computer (visuals)
- `!world.isClient()` - runs on the server (game logic)

## Useful Commands

```bash
# Build the mod
gradle build

# Run Minecraft with your mod
gradle runClient

# Clean build artifacts
gradle clean

# See all available tasks
gradle tasks
```

## The output JAR is at:
`build/libs/tennis-ball-mod-1.0.0.jar`

You can copy this to your Minecraft `mods/` folder to use it in regular Minecraft.
