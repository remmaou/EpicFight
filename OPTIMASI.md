# Catatan optimasi (tanpa menghapus fitur)

Semua perubahan hanya mengurangi pembuatan objek sementara (penyebab GC lag/stutter) dan kerja berulang.
Perilaku/fitur tidak diubah. **Belum dikompilasi penuh** (tidak ada toolchain Minecraft di sini): kirim `build.log` bila ada error.

| File | Perubahan |
|---|---|
| `api/animation/Pose.java` | `orElseEmpty` tidak lagi membuat `JointTransform` baru di setiap panggilan (sebelumnya tiap sendi, tiap frame, tiap mob). Sumber sampah memori terbesar. |
| `api/event/impl/VanillaEntityEventHooks.java` | `preTick`/`postTick` (jalan untuk SEMUA entity tiap tick) tanpa `Optional` + lambda. |
| `api/client/animation/ClientAnimator.java` | `applyBindModifier`: berhenti lebih awal bila tidak ada layer komposit, daftar prioritas dibalik sekali (bukan per sendi). `iterVisibleLayers` tanpa stream. `getLivingMotion` tanpa lookup ganda. |
| `api/client/animation/Layer.java` | `getEnabledPose`: motion dihitung sekali, bukan per sendi. |
| `client/renderer/patched/entity/PatchedLivingEntityRenderer.java` | Render layer: 1x lookup map, bukan 2x per layer per frame. |
| `client/events/engine/RenderEngine.java` | UI entity per mob tiap frame tanpa `Optional` + lambda capturing. |
| `world/capabilities/entitypatch/HumanoidMobPatch.java` | Pathfinding mount/jockey dihitung ulang tiap 5 tick (atau saat path habis), bukan tiap tick. |

Saran pengaturan: dengan 93 mod + Epic Fight, alokasi RAM 512 MB terlalu kecil (lihat PORTING_STATUS.md); naikkan ke 768-1024 MB bila bisa.

## Update: cape & elytra
| File | Perubahan |
|---|---|
| `api/client/physics/cloth/ClothSimulator.java` | Pose penuh + transform collider cape dulu dihitung 6x per frame (tiap substep). Sekarang tiap 2 substep (substep terakhir tetap dihitung, hasil akhir sama). AABB luar collider di-cache per transform (dulu dihitung ulang tiap constraint tiap substep). |
| `client/world/capabilites/entitypatch/player/AbstractClientPlayerPatch.java` | Fix kepala patah ke belakang saat elytra/riptide: badan sudah dimiringkan sesuai pitch kamera, kepala dulu di-pitch lagi sehingga dobel. Sekarang pitch kepala dikurangi sesuai porsi yang sudah diterapkan badan. |

## Update: animasi lebih halus
`ClientAnimator.tick`: perpindahan motion dasar (idle/jalan/lari/jatuh) mendapat blend +0.08 dtk (total ±0,23 dtk, sebelumnya 0,15). Hanya transisi motion hidup; animasi serangan/skill tidak berubah. Ubah `LIVING_MOTION_EXTRA_BLEND` (0 = seperti semula).

## Update: lag saat menyerang mob
| File | Perubahan |
|---|---|
| `client/particle/AbstractTrailParticle.java` | Jejak senjata (trail) memakai `LinkedList` tetapi dibaca dengan `get(i)` di loop render, jadi biaya naik kuadrat per frame (penyebab utama FPS drop saat menebas). Diganti `ArrayList`; 4 objek vektor per segmen dipakai ulang. |
| `client/particle/AnimationTrailParticle.java` | `invisibleTrailEdges` juga `ArrayList`. |
| `particle/HitParticleType.java` | Tidak membuat `Random` baru di setiap hit. |
