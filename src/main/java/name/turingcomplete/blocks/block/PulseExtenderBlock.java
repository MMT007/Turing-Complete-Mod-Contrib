package name.turingcomplete.blocks.block;

import name.turingcomplete.blocks.AbstractLogicBlock;
import name.turingcomplete.init.PropertyInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class PulseExtenderBlock extends AbstractLogicBlock {
    private static final IntProperty DELAY = PropertyInit.DELAY_8;
    private static final IntProperty TIMER = PropertyInit.TIMER;
    private static final IntProperty TICK = PropertyInit.TICK;

    public PulseExtenderBlock(Settings settings) {
        super(settings);
    }


    @Override
    public Boolean dustConnectsToThis(BlockState state, Direction direction) {
        return direction.getAxis() == state.get(FACING).getAxis();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DELAY, TIMER, TICK);
    }

    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld)
            return ActionResult.PASS;

        BlockState next_state = player.isInSneakingPose() ? reverseCycleDelay(state) : state.cycle(DELAY);
        world.setBlockState(pos, next_state, 3);
        return ActionResult.success(world.isClient);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if(getInputActive(world,pos,state,RelativeSide.BACK)){
            world.scheduleBlockTick(pos,this,getUpdateDelay());
        }
    }

    @Override
    protected final int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return direction == state.get(FACING) && state.get(TIMER) > 0 ? 15 : 0;
    }

    //=============================================

    private int getUpdateDelay() {return 2;}

    private BlockState reverseCycleDelay(BlockState state){
        ArrayList<Integer> list = new ArrayList<>(PulseExtenderBlock.DELAY.getValues());
        int current = list.indexOf(state.get(PulseExtenderBlock.DELAY));

        if (current == 0) return state.with(PulseExtenderBlock.DELAY, list.getLast());
        else return state.with(PulseExtenderBlock.DELAY, list.get(current-1));
    }

    //=============================================

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if(getInputActive(world, pos, state, RelativeSide.BACK) != state.get(TIMER) > 0)
            world.scheduleBlockTick(pos, this, 2);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int current_TIMER = state.get(TIMER);
        int current_TICK = state.get(TICK);

        boolean should_power = getInputActive(world,pos,state,RelativeSide.BACK);
        boolean is_powered = current_TIMER > 0;

        // If the current state was off and should turn on, then turn on
        if (!is_powered && should_power){
            world.setBlockState(pos, state.with(TIMER, state.get(DELAY)).with(TICK,5));
            updateOutputBlock(world,pos,state.get(FACING).getOpposite());
            world.scheduleBlockTick(pos, this, getUpdateDelay());
            return;
        }

        // Else If it is still powered, reset to max
        if (should_power){
            world.setBlockState(pos, state.with(TIMER, state.get(DELAY)).with(TICK,5));
            updateOutputBlock(world,pos,state.get(FACING).getOpposite());
            return;
        }



        // Else falloff to off
        current_TICK -= 1;

        // If a tick has finished, Reduce 1 from TIMER
        if (current_TICK <= 0){
            // If the next TIMER state is 0, then don't schedule a block tick
            if (current_TIMER - 1 <= 0){
                world.setBlockState(pos,state.with(TIMER,0));
                updateOutputBlock(world,pos,state.get(FACING).getOpposite());
                return;
            }

            // Else Drop TIMER by 1, reset TICK, and schedule a block tick
            world.setBlockState(pos,state.with(TIMER, current_TIMER-1).with(TICK,5));
            world.scheduleBlockTick(pos, this, getUpdateDelay());
        } else {
            // Else, drop TICK by 1, and schedule a block tick
            world.setBlockState(pos,state.with(TICK,current_TICK));
            world.scheduleBlockTick(pos,this, getUpdateDelay());
        }
    }
}
