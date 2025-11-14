# Code Review Notes for v0.3.2-alpha Update

## Critical Issues

1. **Block placement bypasses item placement logic**  
   `InteractionController.placeBlock` now calls `world.setBlockState(pos, newState)` directly. This skips `BlockItem#place`, so blocks ignore directional placement, block entity data, sound/event hooks, and placement validation (e.g., torches without support, shulker boxes losing contents). Result: many blocks either fail immediately or appear with wrong state. The method should build an `ItemPlacementContext` and call `blockItem.place` / `blockItem.placeOnBlock` so vanilla placement rules run. 【F:src/main/java/com/aicompanion/controller/InteractionController.java†L52-L76】

2. **Combat "stop" command never clears the active target**  
   The new combat flow lets `attack_entity` set a target, but the `stop` action still only halts movement/view. Without calling `controller.getCombatController().clearTarget()`, the AI keeps swinging forever once in range, even after a stop command. That makes it impossible to cancel combat without killing the target or disconnecting. The stop handler should clear the combat target as well. 【F:src/main/java/com/aicompanion/network/MessageHandler.java†L124-L127】【F:src/main/java/com/aicompanion/controller/CombatController.java†L23-L33】

## Additional Observations

- `InventoryController.selectHotbarSlot` is still a stub. Consider either implementing the hotbar swap or documenting the limitation in the API contract so upstream callers know it is a no-op.
- `InteractionController.useItemOnBlock` always constructs the hit result from the block center with `Direction.UP`. Many block interactions depend on the exact hit face; passing through the real face from the request would make this much more reliable.

