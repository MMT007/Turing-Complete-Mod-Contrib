package name.turingcomplete.mixin;

import name.turingcomplete.blocks.ConnectsToRedstone;
import name.turingcomplete.blocks.RedstonePowerState;
import net.minecraft.block.*;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.Direction;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {
    // Used To Connect The Redstone Wire With All The Mod's Components
    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void connectsTo_tcd_mixin(BlockState state, Direction dir, CallbackInfoReturnable<Boolean> e)
    {
        //check for block type
        if (!(state.getBlock() instanceof ConnectsToRedstone logic_block))
            return;

        //check for null direction
        if(dir == null) {
            e.setReturnValue(false);
            e.cancel();
            return;
        }

        // use dustConnectsToThis(state, dir) to determine whether redstone dust should or should not
        // connect to that side of the Logic Gate.
        e.setReturnValue(logic_block.dustConnectsToThis(state, dir));
        e.cancel();
    }

    @Redirect(
        method = "getReceivedRedstonePower",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/block/RedstoneWireBlock;wiresGivePower:Z",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void redirectPowerSets(RedstoneWireBlock instance, boolean value)
    {RedstonePowerState.power(value);}

    @Redirect(
        method = {
            "emitsRedstonePower",
            "getStrongRedstonePower",
            "getWeakRedstonePower"
        },
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/block/RedstoneWireBlock;wiresGivePower:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean redirectPowerGets(RedstoneWireBlock instance){
        return RedstonePowerState.givesPower();
    }
}
