package name.turingcomplete.blocks;

/**
 * @implNote Controls Redstone Wire Like Components So They Don't Power Each Other Through Blocks
 * @see net.minecraft.block.RedstoneWireBlock
 * @see name.turingcomplete.blocks.block.OmniDirectionalRedstoneBridgeBlock
 * @see name.turingcomplete.blocks.redstonepipe.RedstonePipeBlock
 **/
public class RedstonePowerState {
    private static boolean gives_power = true;

    public static boolean givesPower() {return gives_power;}

    public static void power(boolean power)
    {RedstonePowerState.gives_power = power;}

    public static void setPower()
    {RedstonePowerState.gives_power = true;}

    public static void unsetPower()
    {RedstonePowerState.gives_power = false;}
}
