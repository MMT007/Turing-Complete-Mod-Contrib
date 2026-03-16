package name.turingcomplete.blocks.redstonepipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;

import name.turingcomplete.blocks.RedstonePowerState;
import name.turingcomplete.init.BlockInit;
import name.turingcomplete.init.PropertyInit;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class RedstonePipeBlock extends Block {
    @Override
    public MapCodec<RedstonePipeBlock> getCodec() {
        return CODEC;
    }
    public static final MapCodec<RedstonePipeBlock> CODEC = createCodec(RedstonePipeBlock::new);

    public static final EnumProperty<PipeConnection> NORTH = PropertyInit.PIPE_NORTH;
    public static final EnumProperty<PipeConnection> EAST = PropertyInit.PIPE_EAST;
    public static final EnumProperty<PipeConnection> SOUTH = PropertyInit.PIPE_SOUTH;
    public static final EnumProperty<PipeConnection> WEST = PropertyInit.PIPE_WEST;
    public static final EnumProperty<PipeConnection> UP = PropertyInit.PIPE_UP;
    public static final EnumProperty<PipeConnection> DOWN = PropertyInit.PIPE_DOWN;

    public static final IntProperty POWER = PropertyInit.PIPE_POWER;

    public static final Map<Direction, EnumProperty<PipeConnection>> DIRECTION_PROPERTY = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
        )
    );

    private static final Vec3d[] COLORS = Util.make(new Vec3d[31], colors -> {
        for (int i = 0; i <= 30; i++) {
            float f = (float)i / 30.0F;

            float r = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float g = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float b = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);

            colors[i] = new Vec3d(r, g, b);
        }
    });

    private static final double PIPE_GIRTH = 6/16d, PIPE_SIDE_OFFSET = 5/16d;
    private static final Map<BlockState, VoxelShape> SHAPES = Maps.newHashMap();
    private static final VoxelShape CENTER_SHAPE = VoxelShapes.cuboid(Box.of(new Vec3d(0.5,0.5,0.5), PIPE_GIRTH, PIPE_GIRTH, PIPE_GIRTH));

    public RedstonePipeBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(
            this.stateManager
                .getDefaultState()
                .with(NORTH, PipeConnection.DISCONNECTED)
                .with(EAST, PipeConnection.DISCONNECTED)
                .with(SOUTH, PipeConnection.DISCONNECTED)
                .with(WEST, PipeConnection.DISCONNECTED)
                .with(UP, PipeConnection.DISCONNECTED)
                .with(DOWN, PipeConnection.DISCONNECTED)
                .with(POWER, 0)
        );

        for (var blockState : this.getStateManager().getStates())
            if (blockState.get(POWER) == 0)
                SHAPES.put(blockState, this.getShapeForState(blockState));
    }

    private VoxelShape getShapeForState(BlockState state) {
        var voxelShape = CENTER_SHAPE;

        for (var direction : DIRECTIONS) {
            var connection = state.get(DIRECTION_PROPERTY.get(direction));
            if (connection != PipeConnection.CONNECTED) continue;

            var offset = Vec3d.of(direction.getVector()).multiply(PIPE_SIDE_OFFSET);
            var side_box = CENTER_SHAPE.offset(offset.x, offset.y, offset.z);

            voxelShape = VoxelShapes.union(voxelShape, side_box);
        }

        return voxelShape;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
        BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
    ) { return this.getConnectionsState(world, state, pos); }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {return SHAPES.get(state.with(POWER, 0));}

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx)
    {return this.getConnectionsState(ctx.getWorld(), getDefaultState(), ctx.getBlockPos());}

    private BlockState getConnectionsState(BlockView world, BlockState state, BlockPos pos) {
        for (var direction : DIRECTIONS) {
            var connection_property = DIRECTION_PROPERTY.get(direction);

            if (state.get(connection_property) != PipeConnection.DISABLED) {
                state = state.with(
                    connection_property,
                    this.getSideConnection(world, pos, direction)
                );
            }
        }

        return state;
    }

    private PipeConnection getSideConnection(BlockView world, BlockPos pos, Direction direction) {
        var side_pos = pos.offset(direction);
        var side_state = world.getBlockState(side_pos);

        if (connectsTo(world, side_pos, side_state)) {
            if (side_state.isOf(this))
                return side_state.get(DIRECTION_PROPERTY.get(direction.getOpposite())) == PipeConnection.DISABLED ?
                    PipeConnection.DISABLED :
                    PipeConnection.CONNECTED;

            return PipeConnection.CONNECTED;
        }

        return PipeConnection.DISCONNECTED;
    }

    private void update(World world, BlockPos pos, BlockState state) {
        int received_power = this.getReceivedPower(world, state, pos);

        if (state.get(POWER) != received_power) {
            if (world.getBlockState(pos) == state)
                world.setBlockState(pos, state.with(POWER, received_power), Block.NOTIFY_LISTENERS);

            for (var direction : DIRECTIONS) {
                var side_pos = pos.offset(direction);
                var side_state = world.getBlockState(side_pos);

                if(side_state.isOf(this))
                    world.updateNeighbor(side_state, side_pos, this, pos, false);
                else if (side_state.isOpaqueFullCube(world, side_pos))
                    world.updateNeighborsAlways(side_pos, this);
            }
        }
    }

    private int getReceivedPower(World world, BlockState state, BlockPos pos) {
        RedstonePowerState.unsetPower();
        var redstone_power = getReceivedRedstonePower(world, state, pos);
        RedstonePowerState.setPower();

        int adjacent_power = 0;

        if (redstone_power < 15) {
            // Imediate Neighbors
            for (var direction : DIRECTIONS) {
                var side_pos = pos.offset(direction);
                var side_state = world.getBlockState(side_pos);

                // If Not Connected To Side
                if (state.get(DIRECTION_PROPERTY.get(direction)) != PipeConnection.CONNECTED)
                    continue;

                if (side_state.isOf(this)) {
                    // Edge Case, If Other Pipe Is Not Connected To This Pipe
                    if (side_state.get(DIRECTION_PROPERTY.get(direction.getOpposite())) != PipeConnection.CONNECTED)
                        continue;

                    var side_power = side_state.get(POWER);
                    if (side_power > adjacent_power)
                        adjacent_power = side_power;

                // If Connected To A Block
                } else if (side_state.isOpaqueFullCube(world, side_pos)){
                    // Connected Block Neighbors
                    for (var side_direction : DIRECTIONS){
                        var side_side_pos = side_pos.offset(side_direction);
                        var side_side_state = world.getBlockState(side_side_pos);

                        if (side_side_state.isOf(this)){
                            // If Block's Neighbor Pipe is Connected To Same Block
                            if (side_side_state.get(DIRECTION_PROPERTY.get(side_direction.getOpposite())) != PipeConnection.CONNECTED)
                                continue;

                            var side_side_power = side_side_state.get(POWER);
                            if (side_side_power > adjacent_power)
                                adjacent_power = side_side_power;
                        }
                    }
                }
            }
        }

        return Math.max(redstone_power + redstone_power, adjacent_power - 1);
    }

    private int getReceivedRedstonePower(World world, BlockState state, BlockPos pos) {
        var power = 0;

        for (Direction direction : DIRECTIONS) {
            if (state.get(DIRECTION_PROPERTY.get(direction)) != PipeConnection.CONNECTED) continue;
            int emitted_power = world.getEmittedRedstonePower(pos.offset(direction), direction);

            if (emitted_power >= 15) return 15;
            if (emitted_power > power) power = emitted_power;
        }

        return power;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock()) && !world.isClient)
            this.update(world, pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, false);

            if (!world.isClient) this.update(world, pos, state);
        }
    }


    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) this.update(world, pos, state);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {return RedstonePowerState.givesPower();}

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return RedstonePowerState.givesPower() ? getWeakRedstonePower(state, world, pos, direction) : 0;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!RedstonePowerState.givesPower()) return 0;

        var power = state.get(POWER);

        return state.get(DIRECTION_PROPERTY.get(direction)) == PipeConnection.CONNECTED
            ? (int) Math.ceil(power * 0.5f) : 0;
    }

    protected static boolean connectsTo(BlockView view, BlockPos pos,BlockState state) {
        if (state.isOf(BlockInit.REDSTONE_PIPE)) return true;
        return state.isOpaqueFullCube(view,pos);
    }

    public static int getColor(BlockState state) {return getColor(state.get(POWER));}
    public static int getColor(int powerLevel) {
        Vec3d vec3d = COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int power = state.get(POWER);
        if (power != 0 && random.nextFloat() <= 0.0266f * power ){
            var x = (random.nextFloat() - 0.5) * 0.5;
            var y = (random.nextFloat() - 0.5) * 0.5;
            var z = (random.nextFloat() - 0.5) * 0.5;

            world.addParticle(
                new DustParticleEffect(COLORS[power].toVector3f(), 1.0F),
                pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                0.0, 0.0, 0.0
            );
        }
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.with(NORTH, state.get(SOUTH))
                .with(EAST, state.get(WEST))
                .with(SOUTH, state.get(NORTH))
                .with(WEST, state.get(EAST));
            case COUNTERCLOCKWISE_90 -> state.with(NORTH, state.get(EAST))
                .with(EAST, state.get(SOUTH))
                .with(SOUTH, state.get(WEST))
                .with(WEST, state.get(NORTH));
            case CLOCKWISE_90 -> state.with(NORTH, state.get(WEST))
                .with(EAST, state.get(NORTH))
                .with(SOUTH, state.get(EAST))
                .with(WEST, state.get(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.with(NORTH, state.get(SOUTH))
                .with(SOUTH, state.get(NORTH));
            case FRONT_BACK -> state.with(EAST, state.get(WEST))
                .with(WEST, state.get(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, POWER);
    }

    @Override
    protected ActionResult onUse(
        BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit
    ) {
        if (!player.isSneaking()) return ActionResult.PASS;

        var hit_pos = hit.getPos().subtract(
            pos.getX(), pos.getY(), pos.getZ()
        ).multiply(2).subtract(1,1,1);

        var dir = getClickDirection(hit, hit_pos);

        var neighbor_pos = pos.offset(dir);
        var neighbor_state = world.getBlockState(neighbor_pos);

        if (!connectsTo(world, neighbor_pos, neighbor_state)) return ActionResult.PASS;

        var dir_property = DIRECTION_PROPERTY.get(dir);
        var dir_val = state.get(dir_property);

        var toggled_value = dir_val == PipeConnection.DISABLED ? PipeConnection.CONNECTED : PipeConnection.DISABLED;
        world.setBlockState(pos, state.with(dir_property, toggled_value));

        if (neighbor_state.isOf(this)){
            world.setBlockState(neighbor_pos, neighbor_state.with(
                DIRECTION_PROPERTY.get(dir.getOpposite()),
                toggled_value
            ));
        }

        return ActionResult.SUCCESS;
    }

    private static Direction getClickDirection(BlockHitResult blockHitResult, Vec3d hit_pos) {
        double x = hit_pos.x, y = hit_pos.y, z = hit_pos.z;
        var r = Math.sqrt(x * x + y * y + z * z);

        // If Clicked At Center Of Pipe, Return Model's Side Direction
        if (r < 0.375) return blockHitResult.getSide();
        else { // Else Pick From The Model's Clicked Position
            double ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);

            // Pick Dominant Axis
            if (ax > ay && ax > az) return x > 0 ? Direction.EAST : Direction.WEST;
            else if (ay > az) return y > 0 ? Direction.UP : Direction.DOWN;
            else return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }
}
