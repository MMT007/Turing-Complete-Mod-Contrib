package name.turingcomplete.client.mixin;

import name.turingcomplete.blocks.ConnectsToRedstone;
import name.turingcomplete.init.BlockInit;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Unique;
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

    // Copy of RedstoneView::getReceivedRedstonePower
    // Used To EXCLUDE The 4 Way Bridge From The Strong Power Sources
    @Redirect(method = "getReceivedRedstonePower", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I"))
    private int getStrongestRedstonePower(World world, BlockPos pos) {
        int i = 0;

        for(Direction direction : Direction.values()) {
            int j = getEmittedStrongPower(world,pos.offset(direction), direction);

            if (j >= 15) return 15;
            if (j > i) i = j;
        }

        return i;
    }

    //============== UNIQUE METHODS ======================


    @Unique
    private static int getEmittedStrongPower(World world, BlockPos pos, Direction direction){
        // get source block
        BlockState source = world.getBlockState(pos);

        // if the block is solid, check surrounding blocks for strong powering
        if (source.isSolidBlock(world,pos)){
            int power = 0;
            for(Direction sourceNeighbourDirection : Direction.values()){
                BlockPos sourceNeighbourPosition = pos.offset(sourceNeighbourDirection);
                BlockState sourceNeighbour = world.getBlockState(sourceNeighbourPosition);

                // if block is a redstone wire, ignore it
                if(sourceNeighbour.isOf(BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK)) continue;

                // else check if the received power is bigger than the current biggest
                int neighbourPower = sourceNeighbour.getStrongRedstonePower(world,sourceNeighbourPosition,sourceNeighbourDirection);
                if(neighbourPower > power) power = neighbourPower;
            }

            // return biggest power from nearby source
            return power;

        } // else:

        // if blocks is a redstone wire, return it's power with a falloff
        if (source.isOf(BlockInit.OMNI_DIRECTIONAL_REDSTONE_BRIDGE_BLOCK)) return source.getWeakRedstonePower(world,pos,direction) -1;

        // else get weak power emitted from the block
        return source.getWeakRedstonePower(world,pos,direction);
    }
}
