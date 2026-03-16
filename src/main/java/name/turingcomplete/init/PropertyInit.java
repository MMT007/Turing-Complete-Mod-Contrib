package name.turingcomplete.init;

import name.turingcomplete.TuringComplete;
import name.turingcomplete.blocks.redstonepipe.PipeConnection;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;

public class PropertyInit {
    public static final BooleanProperty SWAPPED_DIR = BooleanProperty.of("swapped_direction");
    public static final BooleanProperty POWERED_X = BooleanProperty.of("powered_x");
    public static final BooleanProperty POWERED_Z = BooleanProperty.of("powered_z");

    public static final IntProperty POWER_X = IntProperty.of("power_x",0,15);
    public static final IntProperty POWER_Z = IntProperty.of("power_z",0,15);

    public static final IntProperty DELAY_8 = IntProperty.of("delay", 1,8);
    public static final IntProperty TIMER = IntProperty.of("timer",0,8);
    public static final IntProperty TICK = IntProperty.of("tick",0,5);


    public static final IntProperty PIPE_POWER = IntProperty.of("pipe_power",0,30);
    public static final EnumProperty<PipeConnection> PIPE_NORTH = EnumProperty.of("pipe_north", PipeConnection.class);
    public static final EnumProperty<PipeConnection> PIPE_SOUTH = EnumProperty.of("pipe_south", PipeConnection.class);
    public static final EnumProperty<PipeConnection> PIPE_EAST = EnumProperty.of("pipe_east", PipeConnection.class);
    public static final EnumProperty<PipeConnection> PIPE_WEST = EnumProperty.of("pipe_west", PipeConnection.class);
    public static final EnumProperty<PipeConnection> PIPE_UP = EnumProperty.of("pipe_up", PipeConnection.class);
    public static final EnumProperty<PipeConnection> PIPE_DOWN = EnumProperty.of("pipe_down", PipeConnection.class);

    public static void load(){
        TuringComplete.LOGGER.info("Properties initialised...");
    }
}
