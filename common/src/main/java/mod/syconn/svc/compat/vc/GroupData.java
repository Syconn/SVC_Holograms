package mod.syconn.svc.compat.vc;

import de.maxhenkel.voicechat.api.Group;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class GroupData { // TODO DO I NEED TO SAVE NBT?

    private final String name;
    @Nullable
    private final String password;
    private final Type type;
    @Nullable
    private UUID id;

    public GroupData(String name, @Nullable String password, Type type, @Nullable UUID id) {
        this.name = name;
        this.password = password;
        this.type = type;
        this.id = Objects.requireNonNullElseGet(id, UUID::randomUUID);
    }

    public GroupData(String name, @Nullable String password, Type type) {
        this(name, password, type, null);
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public Type getType() {
        return type;
    }

    public UUID getId() {
        if (id == null) id = UUID.randomUUID();
        return id;
    }

    public enum Type {
        NORMAL(Group.Type.NORMAL),
        OPEN(Group.Type.OPEN),
        ISOLATED(Group.Type.ISOLATED);

        private final Group.Type type;

        Type(Group.Type type) {
            this.type = type;
        }

        public Group.Type getType() {
            return type;
        }

        public static Type fromGroupType(Group.Type type) {
            for (Type t : values()) {
                if (t.getType() == type) return t;
            }
            return NORMAL;
        }
    }
}
