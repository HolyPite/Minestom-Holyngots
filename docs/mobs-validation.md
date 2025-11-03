Mob System Validation Checklist
================================

Automated
---------
- `./gradlew build` once filesystem writes are available to verify the new packages compile.
- Add future unit tests for `MobLootRoller.generateLoot` covering probability edges and item resolution errors.

Manual
------
- Spawn a sample archetype via upcoming `/mob spawn <id>` command and observe:
  - entity spawns with configured equipment and custom name.
  - behaviours tick without throwing errors (action bar/log spam).
  - metadata tag `mmo:mob_archetype` is present on the entity.
- Kill the mob to confirm loot drops once the drop hook is wired.
- Verify boss bar/action bar integrations still function while attacking the mob.

Follow-ups
----------
- Implement respawn timers & quest integration before shipping.
- Extend attribute application beyond base health once stat mapping is defined.
