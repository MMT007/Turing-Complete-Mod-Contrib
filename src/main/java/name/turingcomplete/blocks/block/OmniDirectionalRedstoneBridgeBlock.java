package name.turingcomplete.blocks.block;

import name.turingcomplete.blocks.AbstractLogicBlock;
import name.turingcomplete.init.propertyInit;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class OmniDirectionalRedstoneBridgeBlock extends AbstractLogicBlock {
    private static final IntProperty POWER_Z;
    private static final IntProperty POWER_X;

    private static final Vec3d[] COLORS;

    private boolean wiresGivePower = true;

    public OmniDirectionalRedstoneBridgeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(POWER_X, 0)
                .with(POWER_Z, 0)
        );
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER_X,POWER_Z);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
            .with(POWER_X, this.getReceivedPowerOnAxis(ctx.getWorld(), ctx.getBlockPos(), Direction.Axis.X))
            .with(POWER_Z, this.getReceivedPowerOnAxis(ctx.getWorld(), ctx.getBlockPos(), Direction.Axis.Z));
    }

    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock()) && !world.isClient) {
            // Updates This Block
            this.update(world, pos, state);

            // Updates Above And Bellow Blocks
            for (Direction direction : Direction.Type.VERTICAL) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }

            // Updates Blocks Horizontally
            this.updateNeighborsHorizontally(world,pos);
        }
    }

    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, false);
            // If World Is Client World, Ignore Rest Of Code
            if(world.isClient) return;

            // Update Nearby Blocks
            for (Direction direction : DIRECTIONS)
                world.updateNeighborsAlways(pos.offset(direction), this);

            // Update This AnD Other Wires
            this.update(world, pos, state);
            this.updateNeighborsHorizontally(world, pos);
        }
    }


    //=============================================

    // On Receive Block Update
    private void update(World world, BlockPos pos, BlockState state) {
        // Variable Setup
        int received_x_power = this.getReceivedPowerOnAxis(world, pos, Direction.Axis.X);
        int received_z_power = this.getReceivedPowerOnAxis(world,pos, Direction.Axis.Z);

        // If Received Power On X Axis Different Then Current Power
        if (state.get(POWER_X) != received_x_power){
            // Update Block State (With State Check To Make Sure It Doesn't Update Other Blocks)
            if (world.getBlockState(pos) == state){
                world.setBlockState(pos,state.with(POWER_X,received_x_power),Block.NOTIFY_LISTENERS);
            }

            // Update Blocks On The X Axis
            this.updateTarget(world,pos, Direction.EAST);
            this.updateTarget(world,pos, Direction.WEST);
        }

        // If Received Power On Z Axis Different Then Current Power
        if (state.get(POWER_Z) != received_z_power){
            // Update Block State (With State Check To Make Sure It Doesn't Update Other Blocks)
            if (world.getBlockState(pos) == state){
                world.setBlockState(pos,state.with(POWER_Z,received_z_power),Block.NOTIFY_LISTENERS);
            }

            // Update Blocks On The Z Axis
            this.updateTarget(world,pos, Direction.NORTH);
            this.updateTarget(world,pos, Direction.SOUTH);
        }
    }
    //
    private void updateNeighborsHorizontally(World world, BlockPos pos){
        for (Direction direction : Direction.Type.HORIZONTAL) {
            this.updateNeighbors(world, pos.offset(direction));
        }
    }

    // update Nearby Neighbours
    private void updateNeighbors(World world, BlockPos pos) {
        if (!(world.getBlockState(pos).isOf(this) || world.getBlockState(pos).isOf(Blocks.REDSTONE_WIRE)))
            return;

        world.updateNeighborsAlways(pos, this);

        for(Direction direction : DIRECTIONS)
            world.updateNeighborsAlways(pos.offset(direction), this);
    }

    private void updateTarget(World world, BlockPos pos,Direction direction) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        world.updateNeighbor(blockPos, this, pos);
        world.updateNeighborsExcept(blockPos, this, direction);
    }

    // On Neighbor Update
    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
        if (world.isClient) return;

        if (state.canPlaceAt(world, pos)) {
            this.update(world, pos, state);
        }
    }

    //=============================================

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return this.wiresGivePower;
    }
    @Override
    protected boolean isDirectional() {return false;}
    @Override
    public Boolean dustConnectsToThis(BlockState state, Direction di) {return true;}

    // returns the power received on a certain axis
    private int getReceivedPowerOnAxis(World world, BlockPos pos, Direction.Axis axis){
        // Get Received Power By This Block Position
        this.wiresGivePower = false;
        int received = this.getReceivedRedstonePower(world,pos,axis);
        this.wiresGivePower = true;

        int nearby_power = 0;

        // If the power is 15 don't check for higher power levels nearby
        if (received < 15) {

            // Goes Through All Directions And Checks If It is in the same Axis.
            for (Direction direction : DIRECTIONS) {
                if (direction.getAxis() == axis) {
                    // Variable Setup for block in current direction
                    BlockPos blockPos = pos.offset(direction);
                    BlockState blockState = world.getBlockState(blockPos);

                    // Sets nearby_power If The Block Has a Bigger Power Level Then The Currently Highest
                    nearby_power = Math.max(nearby_power, this.increasePower(blockState, axis));
                }
            }
        }

        // Returns The Highest Signal Between Received Power And Nearby Power Sources
        return Math.max(received, nearby_power - 1);
    }

    private int getReceivedRedstonePower(World world, BlockPos pos, Direction.Axis axis) {
        int power = 0;

        // set directions for queried axis
        Direction[] directions = new Direction[]{};
        if (axis == Direction.Axis.X) directions = new Direction[]{Direction.EAST, Direction.WEST};
        if (axis == Direction.Axis.Z) directions = new Direction[]{Direction.NORTH, Direction.SOUTH};

        // loop through queried directions
        for (Direction direction : directions) {
            // get power source position
            BlockPos blockPos = pos.offset(direction);

            // get power emitted from block at direction
            int emitted_power = this.getEmittedStrongPower(world,blockPos, direction);

            // if power is already at max, return early
            if (emitted_power >= 15) return 15;

            // else, check if it is bigger then the other power sources
            else if (emitted_power > power) power = emitted_power;
        }

        // return biggest power source
        return power;
    }

    private int getEmittedStrongPower(World world, BlockPos pos, Direction direction){
        // get source block
        BlockState source = world.getBlockState(pos);

        // if the block is solid, check surrounding blocks for strong powering
        if (source.isSolidBlock(world,pos)){
            int power = 0;
            for(Direction sourceNeighbourDirection : DIRECTIONS){
                BlockPos sourceNeighbourPosition = pos.offset(sourceNeighbourDirection);
                BlockState sourceNeighbour = world.getBlockState(sourceNeighbourPosition);

                // if block is a redstone wire, ignore it
                if(sourceNeighbour.isOf(Blocks.REDSTONE_WIRE)) continue;

                // else check if the received power is bigger than the current biggest
                int neighbourPower = sourceNeighbour.getStrongRedstonePower(world,sourceNeighbourPosition,sourceNeighbourDirection);
                if(neighbourPower > power) power = neighbourPower;
            }

            // return biggest power from nearby source
            return power;

        } // else:

        // if blocks is a redstone wire, return it's power with a falloff
        if (source.isOf(Blocks.REDSTONE_WIRE)) return source.getWeakRedstonePower(world,pos,direction) -1;

        // else get weak power emitted from the block
        return source.getWeakRedstonePower(world,pos,direction);
    }

    private int increasePower(BlockState state, Direction.Axis axis) {
        if (state.isOf(Blocks.REDSTONE_WIRE))
            return state.get(Properties.POWER);

        if (state.isOf(this))
            if (axis == Direction.Axis.X) return state.get(POWER_X);
            else return state.get(POWER_Z);

        return 0;
    }

    public static int getWireColor(BlockState state, IntProperty property) {
        Vec3d vec3d = COLORS[state.get(property)];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.wiresGivePower ? state.getWeakRedstonePower(world, pos, direction) : 0;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // If The Block Doesn't Give Power, Return 0
        if (!this.wiresGivePower) return 0;

        BlockState block_to_power = world.getBlockState(pos.offset(direction.getOpposite()));

        if (block_to_power.isOf(Blocks.REDSTONE_WIRE)){
            if (direction.getAxis() == Direction.Axis.X)
                return state.get(POWER_X) -1;
            else if (direction.getAxis() == Direction.Axis.Z)
                return state.get(POWER_Z) -1;
        }

        // Returns Respective Power Levels
        if (direction.getAxis() == Direction.Axis.X)
        {
            return state.get(POWER_X);
        }
        else if (direction.getAxis() == Direction.Axis.Z)
        {
            return state.get(POWER_Z);
        }

        // Returns Zero By Default
        return 0;
    }

    static {
        POWER_X = propertyInit.POWER_X;
        POWER_Z = propertyInit.POWER_Z;
        COLORS = Util.make(new Vec3d[16], (colors) -> {
            for(int i = 0; i <= 15; ++i) {
                float f = (float)i / 15.0F;
                float g = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
                float h = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
                float j = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
                colors[i] = new Vec3d(g, h, j);
            }

        });
    }

}
