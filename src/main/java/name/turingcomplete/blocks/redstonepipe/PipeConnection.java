package name.turingcomplete.blocks.redstonepipe;

import net.minecraft.util.StringIdentifiable;

public enum PipeConnection implements StringIdentifiable {
    CONNECTED, DISCONNECTED, DISABLED;

    @Override
    public String asString() {
        return this.name().toLowerCase();
    }
}
