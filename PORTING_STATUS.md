# Epic Fight → Fabric 1.21.1 — porting status

**Not compiled or run yet.** This was written without a Minecraft/Fabric toolchain available, so the first
GitHub Actions run is expected to report compile errors. Send `build.log` (artifact "build-log") back and they get fixed in rounds.

## How to build on GitHub (works from a phone browser)
1. Create a GitHub repository and upload the contents of this folder (keep `.github/workflows/build.yml`).
2. Open the **Actions** tab → "Build Epic Fight (Fabric 1.21.1)" → **Run workflow** (it also runs on every push).
3. When it finishes, download the artifact `epicfight-fabric-jar` (success) or `build-log` (failure).
4. Install with Fabric Loader 0.16+ and Fabric API on Minecraft 1.21.1 (Forge Config API Port is bundled inside the jar).

## Ported (written, unverified)
- Gradle/Loom build, GitHub Actions workflow, `fabric.mod.json`, access widener (converted from the access transformer; field descriptors written by hand).
- Registries: `DeferredRegister`/`DeferredHolder`/`RegistryBuilder`/callbacks shim on top of Fabric registries; vanilla-order registration.
- Entity patch storage (replaces NeoForge attachments) and item capability provider (replaces NeoForge capabilities).
- Combat core via mixins (`platform/fabric/mixin`): incoming damage, damage pre/post, knockback, death, drops (+ skill-book mob drops), shield block, jump,
  mob effects, mount/dismount, entity tick pre/post, join level, enderman spawn/teleport, projectile impact, item-use start, arrow loose, player data load, Ender Dragon size.
- Fabric API events: equipment change, tracking, player clone/dimension change, right-click item, item attribute modifiers, loot tables, datapack sync, commands, reload listeners, skill-book creative tabs.
- Networking (payload registrar / distributor shim on Fabric networking v1), configs (via Forge Config API Port), custom attributes, spawn placements, datapack registry (Emote).
- Client: typed event bus + shim events for all events the render/control engines use, particles, renderers, HUD layers, screen events, shaders, key mappings, built-in legacy pack.

## Build log fixes (round 1)
- Added `jsr305` and `checker-qual` (javax.annotation / checkerframework annotations were missing: ~750 of the 798 errors).
- `ServerEntityEvents` import moved to `net.fabricmc.fabric.api.event.lifecycle.v1`.
- `ModifyItemAttributeModifiersCallback` does not exist in Fabric API 1.21.1: replaced by `MixinItemStackModifiers`.
- `MixinWitherBossAccessor`: overloaded `performRangedAttack` now selected by descriptor.
- Core no longer references Iris (`IrisComputeShaderSetup` moved to `src/compat/iris`, loaded by reflection) or Simply Tooltips.

## Build log fixes (round 2)
- `MixinWitherBossAccessor`: `@Invoker` kembali memakai nama saja (`performRangedAttack`); overload dibedakan oleh signature method accessor.
  Memakai descriptor di dalam `value` membuat annotation processor Mixin gagal ("Could not locate @Invoker target").
- `MixinEntity`: inject ke `Entity#onAddedToLevel` dihapus (method itu tambahan NeoForge, tidak ada di vanilla -> game akan crash saat start).
  Gantinya `VanillaEntityEventHooks.onJoinLevel` memanggil `entitypatch.onAddedToLevel()`.
- `MixinRespawnEvent`: `@Shadow minecraft` dihapus (field itu ada di superclass `ClientCommonPacketListenerImpl`); dipakai `Minecraft.getInstance()`.
- Forge Config API Port sekarang dibundel di dalam jar (`include` / jar-in-jar).
- Workflow: jar `-sources` tidak ikut artifact, error pertama tampil di tab Summary.

## Build log fixes (round 3)
- Semua `getEnchantmentLevel(Holder)` pada ItemStack (extension NeoForge) -> `FabricHolders.getEnchantmentLevel(stack, holder)` = `EnchantmentHelper.getItemEnchantmentLevel`.
- Typo `this.Minecraft.getInstance()` -> `Minecraft.getInstance()` (PopupBox, Grid, Static, SideNavigationBarOpener).
- `EmoteEditScreen.getEntryAtPosition`: harus `public` (access widener membuatnya public).
- Access widener ditambah, setara AT NeoForge: `IdMapper` (nextId/tToId/idToT) dan `GameRules$BooleanValue/IntegerValue#create`.
- `LightningBolt#damage` hanya ada di NeoForge: ditiru lewat `MixinLightningBolt` + `MixinEntityThunderHit` (vanilla selalu 5.0F).
- `CreativeModeTab.Builder#withTabsBefore` hanya NeoForge: dihapus (tab ditaruh di akhir daftar).
- Catatan: javac berhenti di tahap resolusi simbol; setelah ini bisa muncul error tahap berikutnya (round 4).

## Build log fixes (round 4)
- Tinggal 1 error kompilasi: `IdMapper.tToId` bertipe `Reference2IntMap` (bukan `Object2IntMap`) di 1.21.1, jadi baris access widener-nya diperbaiki (diverifikasi dari mapping intermediary Fabric 1.21.1). Catatan: access widener dengan deskriptor salah diabaikan diam-diam oleh Loom, tidak error.

## Build log fixes (round 5)
- Kompilasi Java sudah lolos. `validateAccessWidener` gagal di entri `FolderRepositorySource#createDiscoveredFilePackInfo` (tidak ada di 1.21.1). Entri itu dan `LightningBolt#damage` dihapus, task validasi dimatikan di build.gradle.

## Runtime fixes (round 6)
- Crash saat start: `Trying to access unbound value: epicfight:fracture_block`. Penyebab: urutan `DeferredRegister.registerAll` memakai `BuiltInRegistries.REGISTRY.keySet()` (HashMap, urutan acak), sehingga BLOCK_ENTITY_TYPE bisa didaftarkan sebelum BLOCK. Sekarang memakai iterasi registry (urutan id vanilla).

## Runtime fixes (round 7)
- Crash `ClientConfig.combatCategorizedItems is null` (CLIENT_STARTED): Forge Config API Port memuat config segera saat `register()`, sedangkan listener `loading` dipasang sesudahnya sehingga event pertama terlewat. Listener (Client/Common/Server) sekarang dipasang sebelum `register()`.

## Runtime fixes (round 8) — dari log peluncuran di Android (Fabric Loader 0.20.1, MobileGlues)
Hasil: game sudah sampai menu utama (93 mod), tetapi log menunjukkan satu crash saat resource reload dan beberapa error.
- **Crash resource reload** `Non [a-z0-9/._-] character in path of location: minecraft:shaders/core/epicfight:solid_model.json`
  (`EpicFightShaders.registerShadersEvent`). `new ShaderInstance(provider, "epicfight:solid_model", ...)` hanya berjalan di NeoForge;
  di vanilla 1.21.1 path-nya menjadi `minecraft:shaders/core/epicfight:solid_model.json`. Fabric API hanya menulis ulang namespace untuk program
  yang dibuat lewat `CoreShaderRegistrationCallback` (dicek di source Fabric API 1.21.1, `ShaderProgramMixin`: hanya jika `this instanceof FabricShaderProgram`).
  Sekarang shader didaftarkan lewat `CoreShaderRegistrationCallback.EVENT` di `EpicFightFabricClient`; `EpicFightShaders.registerCoreShaders` menggantikan
  `registerShadersEvent`. Efek samping yang ikut hilang: Minecraft memakai `clearResourcePacksOnError` dan **menghapus semua resource pack terpilih**
  (`options.txt`: `resourcePacks:[]`), shader lama juga tidak ditutup tiap reload (kebocoran GPU).
  `RegisterShadersEvent` tetap dipakai oleh `ComputeShaderProvider` (compute shader; di GLES/MobileGlues otomatis "Unsupported").
- `Missing sound for event: epicfight:sfx.no_sound`: entri kosong `sfx.no_sound` ditambahkan ke `sounds.json`.
- `No data fixer registered for area_effect_breath / dropped_nether_star / wither_skeleton_minion / wither_ghost / fracture_block` (level ERROR, tidak berbahaya):
  pengecekan skema datafixer vanilla (`SharedConstants.CHECK_DATA_FIXER_SCHEMA`) dimatikan hanya selama `registerAll()` Epic Fight, lalu dikembalikan.
- `Failed at loading library configNativeFile`: library native bawaan Linux hanya x86_64. Di Android (os.name=Linux, os.arch=aarch64) sekarang dilewati
  tanpa menulis file `.so` dan tanpa WARN (fitur Epic Skins online memang tidak tersedia di sana, sama seperti sebelumnya).
- Sengaja tidak diubah (tidak berbahaya / bukan dari Epic Fight):
  - `Registry 'epicfight:weapon_data' was empty after loading`: registry ekstensi untuk addon, memang kosong secara default. Pesannya dari validasi registry vanilla, bukan crash.
  - `forgeconfigapiport ... refmap could not be read`, `moreculling ... refmap`, `@Mixin target was not found gnetum...`, `Error loading class xaero/journeymap/sodium...`:
    peringatan dari mod lain (opsional) / Forge Config API Port, tanpa dampak.
  - `UnknownHostException sessionserver.mojang.com / api.minecraftservices.com`, `Failed Fabric Loader update check`, `Couldn't connect to realms`: HP tidak ada internet saat game dibuka.
  - `Error parsing option value off for option Fullscreen`: `options.txt` rusak (ganti `fullscreen:off` menjadi `fullscreen:false`).
  - `Shader rendertype_entity_translucent_emissive could not find sampler named Sampler2`: peringatan vanilla/Sodium.
- Alokasi RAM di log hanya 512 MB (RAM perangkat 1811 MB) untuk 93 mod + Epic Fight. Bila game tertutup sendiri tanpa laporan crash saat masuk dunia,
  kemungkinan besar itu OutOfMemory/low-memory killer Android, bukan bug kode. Naikkan alokasi RAM (mis. 768-1024 MB) atau kurangi mod.

## Runtime fixes (round 9) — klik kiri/kanan mati, tidak ada animasi serang
- **Akar masalah:** vanilla `KeyMapping.MAP` hanya menyimpan SATU mapping per tombol fisik (yang terakhir didaftarkan). Epic Fight mendaftarkan
  `ATTACK` dan `WEAPON_INNATE_SKILL` (klik kiri), `GUARD` (klik kanan), `MOVER_SKILL` (spasi), dst yang bentrok dengan serang/pakai/lompat vanilla.
  NeoForge mengganti `MAP` dengan `KeyMappingLookup` (semua mapping pada tombol itu dapat klik/status tekan). Di Fabric, mapping Epic Fight "mencuri"
  klik sehingga klik kiri/kanan vanilla (hancurkan, pasang, serang) tidak jalan dan input serang Epic Fight tidak terbaca benar.
- **Perbaikan:** `MixinKeyMapping` menimpa `KeyMapping.click/set` agar semua mapping pada tombol yang sama diperbarui (meniru NeoForge).
- `ControlEngine`: bila `playerpatch` masih null saat ada input, diikat ulang dari patch pemain lokal (JOIN bisa terjadi sebelum patch siap).

## Known gaps / TODO
- **Compat modules.** Loader: `compat/CompatLoader` (reflection, only loads a module when its mod is installed).
  - Always in the core build: Better Third Person, Iris (compute-shader init; block renderer used if the Sodium module is built).
  - Optional (`./gradlew build -Pcompat=iris,playeranimator,firstperson`, sources in `src/compat/<module>/java`): Iris+Sodium fake block renderer, PlayerAnimator, First Person Model.
    The GitHub workflow builds each of them as a separate job; a failed one does not affect the core jar. Library versions are in `gradle.properties` (only the Sodium coordinate pattern was checked; the rest are guesses).
  - Still in `_pending/compat` (not ported): Geckolib, AzureLib, SkinLayers3D, Wildfire Female Gender, Simply Tooltips, Controlify, Shoulder Surfing, JEI, KubeJS, Curios (needs Trinkets/Accessories), PlayerRevive, MCreator, Vampirism, Werewolves.
- Fixed: `build.gradle` used to exclude `yesman/epicfight/compat/**`, which also removed `ICompatModule`/`MinecraftMod` that `RenderEngine` imports (compile error).
- Data generators (`_pending/datagen`) removed from the build.
- `Rarity` "unique" (green) falls back to UNCOMMON. Nothing in the code or datapacks references `UNIQUE`, so there is no visible effect today.
- Skill-book icons per category: done with plain item-model overrides (`skillbook.json` + property `epicfight:skill` returning 1..5). Categories added by addons use the default icon.
- Camera roll: applied in `MixinGameRendererEvents` (unverified). `AFTER_TRIPWIRE_BLOCKS` render stage is still approximated by `AFTER_TRANSLUCENT`.
- In-game config: Mod Menu entrypoint `EpicFightModMenu` opens `EpicFightSettingScreen`. The Forge-style config files (`ClientConfig`, `CommonConfig`) can also be edited with Mod Menu + the Configured mod. `modmenu_version=11.0.3` is unverified.
- Unverified guesses to check first if the build/launch complains: Loom version, Forge Config API Port coordinates/package (`fuzs.forgeconfigapiport.fabric.api.neoforge.v4`),
  mixin injection targets (names/descriptors for 1.21.1), access widener descriptors.
- `_neoforge_reference/` keeps the original NeoForge platform code for comparison; it is not compiled.

## Licensing
Code is GPL (see `LICENSE`). Assets (textures, models, animations) are "all rights reserved" (`LICENSE-ASSETS`): fine for personal use, do not redistribute without the Epic Fight team's permission.


## 3D Skin Layers compat (optional module `skinlayers3d`)
- Source: `src/compat/skinlayers3d/java`, built with `-Pcompat=skinlayers3d` against `libs/skinlayers3d-fabric-1.11.4-mc1.21.1.jar`.
- Changes from the NeoForge version: NeoForge attachment -> per-entity weak map (cleaned on `ClientEntityEvents.ENTITY_UNLOAD`, which runs on the client thread so GPU buffers are freed safely);
  mixin accessors -> `VarHandle` (`SkinLayerAccess`); `PlayerSettings#get*Mesh` no longer exists in 1.11.4, so meshes are always rebuilt from the skin texture.
- All referenced classes, fields and method signatures were checked against the 1.11.4 jar. Not compiled or run.


## Runtime findings (from in-game crash reports)
- **Holder equality (fixed):** `DeferredHolder` and the registry's `Holder.Reference` hashed/compared differently, so lookups like
  `skillDataManager.getDataValue(KEY)` returned null (NPE in `ComboAttacks.updateContainer`) and vanilla `AttributeMap` could not find
  Epic Fight attributes. `MixinHolderReference` + `DeferredHolder.equals/hashCode` make them interchangeable, as on NeoForge.
- **Memory:** the report shows `-Xmx512M`. Epic Fight loads large armature/animation data; give the game at least 1.5 GB (and trim other mods) or expect OutOfMemoryError / Android killing the app.

## Runtime fixes (round 9) — senjata tampil kosong di tab kreatif
- **Gejala**: 26 senjata Epic Fight (spear, tachi, greatsword, longsword, uchigatana, bokken tiap bahan) tampil blank di inventaris.
- **Penyebab**: model item memakai loader NeoForge `neoforge:separate_transforms` (model berbeda per konteks: tangan vs ikon GUI/ground/fixed).
  Fabric mengabaikan "loader", dan JSON-nya tidak punya `parent` di level atas, jadi modelnya kosong.
- **Perbaikan data**: tiap `models/item/<senjata>.json` jadi model vanilla (parent+textures+display dari bagian "base"), ditambah 78 model per konteks di
  `models/item/perspective/<senjata>_{gui,fixed,ground}.json` (memakai tekstur `_gui` 16x16, sama seperti perilaku NeoForge).
- **Perbaikan kode**: `SeparateTransformsModels` (muat model tambahan lewat Fabric Model Loading API + pilih model per konteks),
  `MixinItemRendererPerspective` (di `epicfight.mixins.json`, required=false jadi gagal-lunak) dan `ClientHooksShim.handleCameraTransforms(..., stack)` untuk renderer Epic Fight sendiri.

## Runtime fixes (round 10) — trim armor tidak terlihat, trail pedang tidak muncul
- **Armor trim**: trim dulu digambar dengan `armorDecalCutoutNoCull` (depth test EQUAL) tepat setelah armor yang di-skin oleh Epic Fight; kedalamannya tidak identik
  (apalagi di GLES/Android) sehingga seluruh trim ditolak. Sekarang memakai `EpicFightRenderTypes.armorTrimOverlay` (LEQUAL + view-offset Z), dan atlas trim diambil ulang tiap draw.
- **Trail pedang (swing_trail)**: render type `TRAIL_EFFECT` tidak memanggil `RenderSystem.setShader(getParticleShader)` (partikel vanilla melakukannya di `begin()`),
  jadi trail tergambar dengan shader terakhir yang terpasang dan tak terlihat bila tidak ada partikel vanilla di layar. Shader + depth test kini diset eksplisit.
- Tidak dikompilasi/dijalankan di sini; kirim build.log / latest.log bila masih ada masalah (jika trail masih hilang, log akan menunjukkan apakah partikelnya dibuat).

## Runtime fixes (round 11) — trail pedang benar-benar tidak tergambar (akar masalah)
- `ParticleEngine.render` vanilla hanya menggambar tipe di `RENDER_ORDER`; NeoForge menambal loop-nya agar semua `ParticleRenderType` kustom ikut digambar.
  Di Fabric tambalan itu tidak ada, sehingga semua partikel Epic Fight dengan render type sendiri (swing trail, projectile trail, afterimage, dll) hidup tetapi tidak pernah digambar.
- `MixinParticleEngine` kini punya injeksi di akhir `render` yang menggambar semua tipe kustom (meniru patch NeoForge).
