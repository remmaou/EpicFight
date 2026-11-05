package yesman.epicfight.compat.skinlayer3d;

import dev.tr7zw.skinlayers.versionless.render.CustomModelPart;
import dev.tr7zw.skinlayers.versionless.render.CustomizableCube;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/// Pengganti accessor mixin di versi NeoForge: field milik 3D Skin Layers bersifat protected,
/// jadi dibaca lewat VarHandle (static final, sehingga dioptimasi JIT dan tidak ada boxing).
public final class SkinLayerAccess {
    private static final VarHandle X, Y, Z, X_ROT, Y_ROT, Z_ROT, POLYGONS;

    static {
        try {
            MethodHandles.Lookup part = MethodHandles.privateLookupIn(CustomModelPart.class, MethodHandles.lookup());
            X = part.findVarHandle(CustomModelPart.class, "x", float.class);
            Y = part.findVarHandle(CustomModelPart.class, "y", float.class);
            Z = part.findVarHandle(CustomModelPart.class, "z", float.class);
            X_ROT = part.findVarHandle(CustomModelPart.class, "xRot", float.class);
            Y_ROT = part.findVarHandle(CustomModelPart.class, "yRot", float.class);
            Z_ROT = part.findVarHandle(CustomModelPart.class, "zRot", float.class);

            MethodHandles.Lookup cube = MethodHandles.privateLookupIn(CustomizableCube.class, MethodHandles.lookup());
            POLYGONS = cube.findVarHandle(CustomizableCube.class, "polygons", CustomizableCube.Polygon[].class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static float x(CustomModelPart part) { return (float) X.get(part); }
    public static float y(CustomModelPart part) { return (float) Y.get(part); }
    public static float z(CustomModelPart part) { return (float) Z.get(part); }
    public static float xRot(CustomModelPart part) { return (float) X_ROT.get(part); }
    public static float yRot(CustomModelPart part) { return (float) Y_ROT.get(part); }
    public static float zRot(CustomModelPart part) { return (float) Z_ROT.get(part); }
    public static CustomizableCube.Polygon[] polygons(CustomizableCube cube) { return (CustomizableCube.Polygon[]) POLYGONS.get(cube); }

    private SkinLayerAccess() {}
}
