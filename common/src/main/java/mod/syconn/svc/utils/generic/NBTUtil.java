package mod.syconn.svc.utils.generic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class NBTUtil {

    public static <T> List<T> getList(CompoundTag tag, Function<CompoundTag, T> function) {
        var list = new ArrayList<T>();
        for (int i = 0; i < tag.getInt("len"); i++) list.add(function.apply(tag.getCompound(String.valueOf(i))));
        return list;
    }

    public static <T> CompoundTag putList(List<T> elements, Function<T, CompoundTag> function) {
        var tag = new CompoundTag();
        for (int i = 0; i < elements.size(); i++) tag.put(String.valueOf(i), function.apply(elements.get(i)));
        tag.putInt("len", elements.size());
        return tag;
    }

    public static <T> Set<T> getSet(CompoundTag tag, Function<CompoundTag, T> function) {
        var set = new HashSet<T>();
        for (int i = 0; i < tag.getInt("len"); i++) set.add(function.apply(tag.getCompound(String.valueOf(i))));
        return set;
    }

    public static <T> CompoundTag putSet(Set<T> elements, Function<T, CompoundTag> function) {
        var tag = new CompoundTag();
        var index = 0;
        for (var element : elements) tag.put(Integer.toString(index++), function.apply(element));
        tag.putInt("len", index);
        return tag;
    }

    public static <K, V> Map<K, V> getMap(CompoundTag tag, Function<CompoundTag, K> keyFunction, Function<CompoundTag, V> valFunction) {
        var map = new HashMap<K, V>();
        tag.getList("map", Tag.TAG_COMPOUND).forEach(nbt -> {
            var data = (CompoundTag) nbt;
            map.put(keyFunction.apply(data.getCompound("key")), valFunction.apply(data.getCompound("value")));
        });
        return map;
    }

    public static <K, V> CompoundTag putMap(Map<K, V> elements, Function<K, CompoundTag> keyFunction, Function<V, CompoundTag> valFunction) {
        var map = new CompoundTag();
        var list = new ListTag();
        elements.forEach((k, v) -> {
            var tag = new CompoundTag();
            tag.put("key", keyFunction.apply(k));
            tag.put("value", valFunction.apply(v));
            list.add(tag);
        });
        map.put("map", list);
        return map;
    }

    public @Nullable static <T> T getNullable(CompoundTag tag, Function<CompoundTag, T> function) {
        if (tag.contains("nullable")) return function.apply(tag.getCompound("nullable"));
        return null;
    }

    public static <T> CompoundTag putNullable(@Nullable T optional, Function<T, CompoundTag> function) {
        var tag = new CompoundTag();
        if (optional != null) tag.put("nullable", function.apply(optional));
        return tag;
    }

    public static CompoundTag convert(Consumer<CompoundTag> consumer) {
        var tag = new CompoundTag();
        consumer.accept(tag);
        return tag;
    }

    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, CompoundTag tag) {
        return enumClass.getEnumConstants()[tag.getInt("enumValue")];
    }

    public static CompoundTag putEnum(Enum<?> value) {
        var tag = new CompoundTag();
        tag.putInt("enumValue", value.ordinal());
        return tag;
    }

    public static CompoundTag putUUID(UUID uuid) {
        return NBTUtil.convert(t -> t.putUUID("id", uuid));
    }

    public static ResourceLocation getResourceLocation(CompoundTag tag) {
        return new ResourceLocation(tag.getString("resourceLocation"));
    }

    public static CompoundTag putResourceLocation(ResourceLocation location) {
        return NBTUtil.convert(t -> t.putString("resourceLocation", location.toString()));
    }

    public static UUID getUUID(CompoundTag tag) {
        return tag.getUUID("id");
    }

    public static Vec3 getVec3(CompoundTag tag) {
        return new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
    }

    public static CompoundTag putVec3(Vec3 vec3) {
        var tag = new CompoundTag();
        tag.putDouble("x", vec3.x);
        tag.putDouble("y", vec3.y);
        tag.putDouble("z", vec3.z);
        return tag;
    }
}
