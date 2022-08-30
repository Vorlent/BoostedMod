package org.boosted.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UnsupportedEntity extends Entity {

    public UnsupportedEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public RuntimeException unsupportedOperation() {
        return new UnsupportedOperationException();
    }

    public boolean collidesWithStateAtPos(BlockPos pos, BlockState state) {
        throw unsupportedOperation();
    }

    public int getTeamColorValue() {
        throw unsupportedOperation();
    }

    public boolean isSpectator() {
        throw unsupportedOperation();
    }

    public void updateTrackedPosition(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public void updateTrackedPosition(Vec3d pos) {
        throw unsupportedOperation();
    }

    public Vec3d getTrackedPosition() {
        throw unsupportedOperation();
    }

    public EntityType<?> getType() {
        throw unsupportedOperation();
    }

    @Override
    public int getId() {
        throw unsupportedOperation();
    }

    public void setId(int id) {
        throw unsupportedOperation();
    }

    public Set<String> getScoreboardTags() {
        throw unsupportedOperation();
    }

    public boolean addScoreboardTag(String tag) {
        throw unsupportedOperation();
    }
    public boolean removeScoreboardTag(String tag) {
        throw unsupportedOperation();
    }

    public void kill() {
        throw unsupportedOperation();
    }

    protected void initDataTracker() {
        throw unsupportedOperation();
    }

    public DataTracker getDataTracker() {
        throw unsupportedOperation();
    }

    public boolean equals(Object o) {throw unsupportedOperation();
    }

    public int hashCode() {
        throw unsupportedOperation();
    }

    public void remove(net.minecraft.entity.Entity.RemovalReason reason) {
        throw unsupportedOperation();
    }

    public void onRemoved() {
        throw unsupportedOperation();
    }

    public void setPose(EntityPose pose) {
        throw unsupportedOperation();
    }

    public EntityPose getPose() {
        throw unsupportedOperation();
    }

    public boolean isInRange(net.minecraft.entity.Entity other, double radius) {
        throw unsupportedOperation();
    }

    protected void setRotation(float yaw, float pitch) {
        throw unsupportedOperation();
    }

    public void setPosition(double x, double y, double z) {
        throw unsupportedOperation();
    }

    protected Box calculateBoundingBox() {
        throw unsupportedOperation();
    }

    protected void refreshPosition() {
        throw unsupportedOperation();
    }

    public void changeLookDirection(double cursorDeltaX, double cursorDeltaY) {
        throw unsupportedOperation();
    }

    public void tick() {
        throw unsupportedOperation();
    }

    public void baseTick() {
        throw unsupportedOperation();
    }

    public void setOnFire(boolean onFire) {
        throw unsupportedOperation();
    }

    public void attemptTickInVoid() {
        throw unsupportedOperation();
    }

    public void resetNetherPortalCooldown() {
        throw unsupportedOperation();
    }

    public boolean hasNetherPortalCooldown() {
        throw unsupportedOperation();
    }

    protected void tickNetherPortalCooldown() {
        throw unsupportedOperation();
    }

    public int getMaxNetherPortalTime() {
        throw unsupportedOperation();
    }

    public void setOnFireFromLava() {
        throw unsupportedOperation();
    }

    public void setOnFireFor(int seconds) {
        throw unsupportedOperation();
    }

    public void setFireTicks(int fireTicks) {
        throw unsupportedOperation();
    }

    public int getFireTicks() {
        throw unsupportedOperation();
    }

    public void extinguish() {
        throw unsupportedOperation();
    }

    protected void tickInVoid() {
        throw unsupportedOperation();
    }

    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        throw unsupportedOperation();
    }

    private boolean doesNotCollide(Box box) {
        throw unsupportedOperation();
    }

    public void setOnGround(boolean onGround) {
        throw unsupportedOperation();
    }

    public boolean isOnGround() {
        throw unsupportedOperation();
    }

    public void move(MovementType movementType, Vec3d movement) {
        throw unsupportedOperation();
    }

    protected boolean hasCollidedSoftly(Vec3d adjustedMovement) {
        throw unsupportedOperation();
    }

    protected void tryCheckBlockCollision() {
        throw unsupportedOperation();
    }

    protected void playExtinguishSound() {
        throw unsupportedOperation();
    }

    protected void addAirTravelEffects() {
        throw unsupportedOperation();
    }

    public BlockPos getLandingPos() {
        throw unsupportedOperation();
    }

    protected float getJumpVelocityMultiplier() {
        throw unsupportedOperation();
    }

    protected float getVelocityMultiplier() {
        throw unsupportedOperation();
    }

    protected BlockPos getVelocityAffectingPos() {
        throw unsupportedOperation();
    }

    protected Vec3d adjustMovementForSneaking(Vec3d movement, MovementType type) {
        throw unsupportedOperation();
    }

    protected Vec3d adjustMovementForPiston(Vec3d movement) {
        throw unsupportedOperation();
    }

    private double calculatePistonMovementFactor(Direction.Axis axis, double offsetFactor) {
        throw unsupportedOperation();
    }

    private Vec3d adjustMovementForCollisions(Vec3d movement) {
        throw unsupportedOperation();
    }

    protected float calculateNextStepSoundDistance() {
        throw unsupportedOperation();
    }

    protected SoundEvent getSwimSound() {
        throw unsupportedOperation();
    }

    protected SoundEvent getSplashSound() {
        throw unsupportedOperation();
    }

    protected SoundEvent getHighSpeedSplashSound() {
        throw unsupportedOperation();
    }

    protected void checkBlockCollision() {
        throw unsupportedOperation();
    }

    protected void onBlockCollision(BlockState state) {
        throw unsupportedOperation();
    }

    public void emitGameEvent(GameEvent event, @Nullable net.minecraft.entity.Entity entity, BlockPos pos) {
        throw unsupportedOperation();
    }

    public void emitGameEvent(GameEvent event, @Nullable net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public void emitGameEvent(GameEvent event, BlockPos pos) {
        throw unsupportedOperation();
    }

    public void emitGameEvent(GameEvent event) {
        throw unsupportedOperation();
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        throw unsupportedOperation();
    }

    private void playAmethystChimeSound(BlockState state) {
        throw unsupportedOperation();
    }

    protected void playSwimSound(float volume) {
        throw unsupportedOperation();
    }

    protected void addFlapEffects() {
        throw unsupportedOperation();
    }

    protected boolean hasWings() {
        throw unsupportedOperation();
    }

    public void playSound(SoundEvent sound, float volume, float pitch) {
        throw unsupportedOperation();
    }

    public boolean isSilent() {
        throw unsupportedOperation();
    }

    public void setSilent(boolean silent) {
        throw unsupportedOperation();
    }

    public boolean hasNoGravity() {
        throw unsupportedOperation();
    }

    public void setNoGravity(boolean noGravity) {
        throw unsupportedOperation();
    }

    protected net.minecraft.entity.Entity.MoveEffect getMoveEffect() {
        throw unsupportedOperation();
    }

    public boolean occludeVibrationSignals() {
        throw unsupportedOperation();
    }

    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
        throw unsupportedOperation();
    }

    public boolean isFireImmune() {
        throw unsupportedOperation();
    }

    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        throw unsupportedOperation();
    }

    public boolean isTouchingWater() {
        throw unsupportedOperation();
    }

    private boolean isBeingRainedOn() {
        throw unsupportedOperation();
    }

    private boolean isInsideBubbleColumn() {
        throw unsupportedOperation();
    }

    public boolean isTouchingWaterOrRain() {
        throw unsupportedOperation();
    }

    public boolean isWet() {
        throw unsupportedOperation();
    }

    public boolean isInsideWaterOrBubbleColumn() {
        throw unsupportedOperation();
    }

    public boolean isSubmergedInWater() {
        throw unsupportedOperation();
    }

    public void updateSwimming() {
        throw unsupportedOperation();
    }

    protected boolean updateWaterState() {
        throw unsupportedOperation();
    }

    void checkWaterState() {
        throw unsupportedOperation();
    }

    private void updateSubmergedInWaterState() {
        throw unsupportedOperation();
    }

    protected void onSwimmingStart() {
        throw unsupportedOperation();
    }

    protected BlockState getLandingBlockState() {
        throw unsupportedOperation();
    }

    public boolean shouldSpawnSprintingParticles() {
        throw unsupportedOperation();
    }

    protected void spawnSprintingParticles() {
        throw unsupportedOperation();
    }

    public boolean isSubmergedIn(TagKey<Fluid> fluidTag) {
        throw unsupportedOperation();
    }

    public boolean isInLava() {
        throw unsupportedOperation();
    }

    public void updateVelocity(float speed, Vec3d movementInput) {
        throw unsupportedOperation();
    }

    public float getBrightnessAtEyes() {
        throw unsupportedOperation();
    }

    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        throw unsupportedOperation();
    }

    public void updatePosition(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public void refreshPositionAfterTeleport(Vec3d pos) {
        throw unsupportedOperation();
    }

    public void refreshPositionAfterTeleport(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public void refreshPositionAndAngles(BlockPos pos, float yaw, float pitch) {
        throw unsupportedOperation();
    }

    public void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        throw unsupportedOperation();
    }

    public float distanceTo(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public double squaredDistanceTo(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public double squaredDistanceTo(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public double squaredDistanceTo(Vec3d vector) {
        throw unsupportedOperation();
    }

    public void onPlayerCollision(PlayerEntity player) {
        throw unsupportedOperation();
    }

    public void pushAwayFrom(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public void addVelocity(double deltaX, double deltaY, double deltaZ) {
        throw unsupportedOperation();
    }

    protected void scheduleVelocityUpdate() {
        throw unsupportedOperation();
    }

    public boolean damage(DamageSource source, float amount) {
        throw unsupportedOperation();
    }

    public float getPitch(float tickDelta) {
        throw unsupportedOperation();
    }

    public float getYaw(float tickDelta) {
        throw unsupportedOperation();
    }

    public Vec3d getClientCameraPosVec(float tickDelta) {
        throw unsupportedOperation();
    }

    public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
        throw unsupportedOperation();
    }

    public boolean collides() {
        throw unsupportedOperation();
    }

    public boolean isPushable() {
        throw unsupportedOperation();
    }

    public void updateKilledAdvancementCriterion(net.minecraft.entity.Entity entityKilled, int score, DamageSource damageSource) {
        throw unsupportedOperation();
    }

    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        throw unsupportedOperation();
    }

    public boolean shouldRender(double distance) {
        throw unsupportedOperation();
    }

    public boolean saveSelfNbt(NbtCompound nbt) {
        throw unsupportedOperation();
    }

    public boolean saveNbt(NbtCompound nbt) {
        throw unsupportedOperation();
    }

    public NbtCompound writeNbt(NbtCompound nbt) {
        throw unsupportedOperation();
    }

    public void readNbt(NbtCompound nbt) {
        throw unsupportedOperation();
    }

    protected boolean shouldSetPositionOnLoad() {
        throw unsupportedOperation();
    }

    protected void readCustomDataFromNbt(NbtCompound var1) {
        throw unsupportedOperation();
    }

    protected void writeCustomDataToNbt(NbtCompound var1) {
        throw unsupportedOperation();
    }

    protected NbtList toNbtList(double ... values) {
        throw unsupportedOperation();
    }

    protected NbtList toNbtList(float ... values) {
        throw unsupportedOperation();
    }

    @Nullable
    public ItemEntity dropItem(ItemConvertible item) {
        throw unsupportedOperation();
    }

    @Nullable
    public ItemEntity dropItem(ItemConvertible item, int yOffset) {
        throw unsupportedOperation();
    }

    @Nullable
    public ItemEntity dropStack(ItemStack stack) {
        throw unsupportedOperation();
    }

    @Nullable
    public ItemEntity dropStack(ItemStack stack, float yOffset) {
        throw unsupportedOperation();
    }

    public boolean isAlive() {
        throw unsupportedOperation();
    }

    public boolean isInsideWall() {
        throw unsupportedOperation();
    }

    public ActionResult interact(PlayerEntity player, Hand hand) {
        throw unsupportedOperation();
    }

    public boolean collidesWith(net.minecraft.entity.Entity other) {
        throw unsupportedOperation();
    }

    public boolean isCollidable() {
        throw unsupportedOperation();
    }

    public void tickRiding() {
        throw unsupportedOperation();
    }

    public void updatePassengerPosition(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    private void updatePassengerPosition(net.minecraft.entity.Entity passenger, net.minecraft.entity.Entity.PositionUpdater positionUpdater) {
        throw unsupportedOperation();
    }

    public void onPassengerLookAround(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    public double getHeightOffset() {
        throw unsupportedOperation();
    }

    public double getMountedHeightOffset() {
        throw unsupportedOperation();
    }

    public boolean startRiding(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public boolean isLiving() {
        throw unsupportedOperation();
    }

    public boolean startRiding(net.minecraft.entity.Entity entity, boolean force) {
        throw unsupportedOperation();
    }

    protected boolean canStartRiding(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    protected boolean wouldPoseNotCollide(EntityPose pose) {
        throw unsupportedOperation();
    }

    public void removeAllPassengers() {
        throw unsupportedOperation();
    }

    public void dismountVehicle() {
        throw unsupportedOperation();
    }

    public void stopRiding() {
        throw unsupportedOperation();
    }

    protected void addPassenger(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    protected void removePassenger(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    protected boolean canAddPassenger(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps, boolean interpolate) {
        throw unsupportedOperation();
    }

    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        throw unsupportedOperation();
    }

    public float getTargetingMargin() {
        throw unsupportedOperation();
    }

    public Vec3d getRotationVector() {
        throw unsupportedOperation();
    }

    public Vec3d getHandPosOffset(Item item) {
        throw unsupportedOperation();
    }

    public Vec2f getRotationClient() {
        throw unsupportedOperation();
    }

    public Vec3d getRotationVecClient() {
        throw unsupportedOperation();
    }

    public void setInNetherPortal(BlockPos pos) {
        throw unsupportedOperation();
    }

    protected void tickNetherPortal() {
        throw unsupportedOperation();
    }

    public int getDefaultNetherPortalCooldown() {
        throw unsupportedOperation();
    }

    public void setVelocityClient(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public void handleStatus(byte status) {
        throw unsupportedOperation();
    }

    public void animateDamage() {
        throw unsupportedOperation();
    }

    public Iterable<ItemStack> getItemsHand() {
        throw unsupportedOperation();
    }

    public Iterable<ItemStack> getArmorItems() {
        throw unsupportedOperation();
    }

    public Iterable<ItemStack> getItemsEquipped() {
        throw unsupportedOperation();
    }

    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        throw unsupportedOperation();
    }

    public boolean isOnFire() {
        throw unsupportedOperation();
    }

    public boolean hasVehicle() {
        throw unsupportedOperation();
    }

    public boolean hasPassengers() {
        throw unsupportedOperation();
    }

    public boolean canBeRiddenInWater() {
        throw unsupportedOperation();
    }

    public void setSneaking(boolean sneaking) {
        throw unsupportedOperation();
    }

    public boolean isSneaking() {
        throw unsupportedOperation();
    }

    public boolean bypassesSteppingEffects() {
        throw unsupportedOperation();
    }

    public boolean bypassesLandingEffects() {
        throw unsupportedOperation();
    }

    public boolean isSneaky() {
        throw unsupportedOperation();
    }

    public boolean isDescending() {
        throw unsupportedOperation();
    }

    public boolean isInSneakingPose() {
        throw unsupportedOperation();
    }

    public boolean isSprinting() {
        throw unsupportedOperation();
    }

    public void setSprinting(boolean sprinting) {
        throw unsupportedOperation();
    }

    public boolean isSwimming() {
        throw unsupportedOperation();
    }

    public boolean isInSwimmingPose() {
        throw unsupportedOperation();
    }

    public boolean shouldLeaveSwimmingPose() {
        throw unsupportedOperation();
    }

    public void setSwimming(boolean swimming) {
        throw unsupportedOperation();
    }

    public boolean isGlowing() {
        throw unsupportedOperation();
    }

    public boolean isInvisible() {
        throw unsupportedOperation();
    }

    public boolean isInvisibleTo(PlayerEntity player) {
        throw unsupportedOperation();
    }

    @Nullable
    public EntityGameEventHandler getGameEventHandler() {
        throw unsupportedOperation();
    }

    @Nullable
    public AbstractTeam getScoreboardTeam() {
        throw unsupportedOperation();
    }

    public boolean isTeammate(net.minecraft.entity.Entity other) {
        throw unsupportedOperation();
    }

    public boolean isTeamPlayer(AbstractTeam team) {
        throw unsupportedOperation();
    }

    public void setInvisible(boolean invisible) {
        throw unsupportedOperation();
    }

    protected boolean getFlag(int index) {
        throw unsupportedOperation();
    }

    protected void setFlag(int index, boolean value) {
        throw unsupportedOperation();
    }

    public int getMaxAir() {
        throw unsupportedOperation();
    }

    public int getAir() {
        throw unsupportedOperation();
    }

    public void setAir(int air) {
        throw unsupportedOperation();
    }

    public int getFrozenTicks() {
        throw unsupportedOperation();
    }

    public void setFrozenTicks(int frozenTicks) {
        throw unsupportedOperation();
    }

    public float getFreezingScale() {
        throw unsupportedOperation();
    }

    public boolean isFrozen() {
        throw unsupportedOperation();
    }

    public int getMinFreezeDamageTicks() {
        throw unsupportedOperation();
    }

    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        throw unsupportedOperation();
    }

    public void onBubbleColumnSurfaceCollision(boolean drag) {
        throw unsupportedOperation();
    }

    public void onBubbleColumnCollision(boolean drag) {
        throw unsupportedOperation();
    }

    public void onKilledOther(ServerWorld world, LivingEntity other) {
        throw unsupportedOperation();
    }

    public void onLanding() {
        throw unsupportedOperation();
    }

    protected void pushOutOfBlocks(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public void slowMovement(BlockState state, Vec3d multiplier) {
        throw unsupportedOperation();
    }

    @Override
    public Text getName() {
        throw unsupportedOperation();
    }

    protected Text getDefaultName() {
        throw unsupportedOperation();
    }

    public boolean isPartOf(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public float getHeadYaw() {
        throw unsupportedOperation();
    }

    public void setHeadYaw(float headYaw) {
        throw unsupportedOperation();
    }

    public void setBodyYaw(float bodyYaw) {
        throw unsupportedOperation();
    }

    public boolean isAttackable() {
        throw unsupportedOperation();
    }

    public boolean handleAttack(net.minecraft.entity.Entity attacker) {
        throw unsupportedOperation();
    }

    public String toString() {
        throw unsupportedOperation();
    }

    public boolean isInvulnerableTo(DamageSource damageSource) {
        throw unsupportedOperation();
    }

    public boolean isInvulnerable() {
        throw unsupportedOperation();
    }

    public void setInvulnerable(boolean invulnerable) {
        throw unsupportedOperation();
    }

    public void copyPositionAndRotation(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public void copyFrom(net.minecraft.entity.Entity original) {
        throw unsupportedOperation();
    }

    @Nullable
    public net.minecraft.entity.Entity moveToWorld(ServerWorld destination) {
        throw unsupportedOperation();
    }

    protected void removeFromDimension() {
        throw unsupportedOperation();
    }

    @Nullable
    protected TeleportTarget getTeleportTarget(ServerWorld destination) {
        throw unsupportedOperation();
    }

    protected Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        throw unsupportedOperation();
    }

    protected Optional<BlockLocating.Rectangle> getPortalRect(ServerWorld destWorld, BlockPos destPos, boolean destIsNether, WorldBorder worldBorder) {
        throw unsupportedOperation();
    }

    public boolean canUsePortals() {
        throw unsupportedOperation();
    }

    public float getEffectiveExplosionResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState, float max) {
        throw unsupportedOperation();
    }

    public boolean canExplosionDestroyBlock(Explosion explosion, BlockView world, BlockPos pos, BlockState state, float explosionPower) {
        throw unsupportedOperation();
    }

    public int getSafeFallDistance() {
        throw unsupportedOperation();
    }

    public boolean canAvoidTraps() {
        throw unsupportedOperation();
    }

    public void populateCrashReport(CrashReportSection section) {
        throw unsupportedOperation();
    }

    public boolean doesRenderOnFire() {
        throw unsupportedOperation();
    }

    public void setUuid(UUID uuid) {
        throw unsupportedOperation();
    }

    @Override
    public UUID getUuid() {
        throw unsupportedOperation();
    }

    public String getUuidAsString() {
        throw unsupportedOperation();
    }

    public String getEntityName() {
        throw unsupportedOperation();
    }

    public boolean isPushedByFluids() {
        throw unsupportedOperation();
    }

    @Override
    public Text getDisplayName() {
        throw unsupportedOperation();
    }

    public void setCustomName(@Nullable Text name) {
        throw unsupportedOperation();
    }

    @Override
    @Nullable
    public Text getCustomName() {
        throw unsupportedOperation();
    }

    @Override
    public boolean hasCustomName() {
        throw unsupportedOperation();
    }

    public void setCustomNameVisible(boolean visible) {
        throw unsupportedOperation();
    }

    public boolean isCustomNameVisible() {
        throw unsupportedOperation();
    }

    public void requestTeleportAndDismount(double destX, double destY, double destZ) {
        throw unsupportedOperation();
    }

    public void requestTeleport(double destX, double destY, double destZ) {
        throw unsupportedOperation();
    }

    public boolean shouldRenderName() {
        throw unsupportedOperation();
    }

    public void onTrackedDataSet(TrackedData<?> data) {
        throw unsupportedOperation();
    }

    public void calculateDimensions() {
        throw unsupportedOperation();
    }

    public Direction getHorizontalFacing() {
        throw unsupportedOperation();
    }

    public Direction getMovementDirection() {
        throw unsupportedOperation();
    }

    protected HoverEvent getHoverEvent() {
        throw unsupportedOperation();
    }

    public boolean canBeSpectated(ServerPlayerEntity spectator) {
        throw unsupportedOperation();
    }

    public Box getVisibilityBoundingBox() {
        return this.getBoundingBox();
    }

    protected Box calculateBoundsForPose(EntityPose pos) {
        throw unsupportedOperation();
    }

    protected float getEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        throw unsupportedOperation();
    }

    public float getEyeHeight(EntityPose pose) {
        throw unsupportedOperation();
    }

    public Vec3d getLeashOffset() {
        throw unsupportedOperation();
    }

    public StackReference getStackReference(int mappedIndex) {
        throw unsupportedOperation();
    }

    @Override
    public void sendSystemMessage(Text message, UUID sender) {
        throw unsupportedOperation();
    }

    public World getEntityWorld() {
        throw unsupportedOperation();
    }

    @Nullable
    public MinecraftServer getServer() {
        throw unsupportedOperation();
    }

    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        throw unsupportedOperation();
    }

    public boolean isImmuneToExplosion() {
        throw unsupportedOperation();
    }

    public void applyDamageEffects(LivingEntity attacker, net.minecraft.entity.Entity target) {
        throw unsupportedOperation();
    }

    public void onStartedTrackingBy(ServerPlayerEntity player) {
        throw unsupportedOperation();
    }

    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        throw unsupportedOperation();
    }

    public float applyRotation(BlockRotation rotation) {
        throw unsupportedOperation();
    }

    public float applyMirror(BlockMirror mirror) {
        throw unsupportedOperation();
    }

    public boolean entityDataRequiresOperator() {
        throw unsupportedOperation();
    }

    @Nullable
    public net.minecraft.entity.Entity getPrimaryPassenger() {
        throw unsupportedOperation();
    }

    @Nullable
    public net.minecraft.entity.Entity getFirstPassenger() {
        throw unsupportedOperation();
    }

    public boolean hasPassenger(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    public boolean hasPassengerType(Predicate<net.minecraft.entity.Entity> predicate) {
        throw unsupportedOperation();
    }

    private Stream<net.minecraft.entity.Entity> streamIntoPassengers() {
        throw unsupportedOperation();
    }

    public Stream<net.minecraft.entity.Entity> streamSelfAndPassengers() {
        throw unsupportedOperation();
    }

    public Stream<net.minecraft.entity.Entity> streamPassengersAndSelf() {
        throw unsupportedOperation();
    }

    public Iterable<net.minecraft.entity.Entity> getPassengersDeep() {
        throw unsupportedOperation();
    }

    public boolean hasPlayerRider() {
        throw unsupportedOperation();
    }

    public net.minecraft.entity.Entity getRootVehicle() {
        throw unsupportedOperation();
    }

    public boolean isConnectedThroughVehicle(net.minecraft.entity.Entity entity) {
        throw unsupportedOperation();
    }

    public boolean hasPassengerDeep(net.minecraft.entity.Entity passenger) {
        throw unsupportedOperation();
    }

    public boolean isLogicalSideForUpdatingMovement() {
        throw unsupportedOperation();
    }

    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        throw unsupportedOperation();
    }

    @Nullable
    public net.minecraft.entity.Entity getVehicle() {
        throw unsupportedOperation();
    }

    public PistonBehavior getPistonBehavior() {
        throw unsupportedOperation();
    }

    public SoundCategory getSoundCategory() {
        throw unsupportedOperation();
    }

    protected int getBurningDuration() {
        throw unsupportedOperation();
    }

    public ServerCommandSource getCommandSource() {
        throw unsupportedOperation();
    }

    protected int getPermissionLevel() {
        throw unsupportedOperation();
    }

    public boolean hasPermissionLevel(int permissionLevel) {
        throw unsupportedOperation();
    }

    @Override
    public boolean shouldReceiveFeedback() {
        throw unsupportedOperation();
    }

    @Override
    public boolean shouldTrackOutput() {
        throw unsupportedOperation();
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        throw unsupportedOperation();
    }

    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        throw unsupportedOperation();
    }

    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        throw unsupportedOperation();
    }

    public boolean isRegionUnloaded() {
        throw unsupportedOperation();
    }

    public double getFluidHeight(TagKey<Fluid> fluid) {
        throw unsupportedOperation();
    }

    public double getSwimHeight() {
        throw unsupportedOperation();
    }

    public Packet<?> createSpawnPacket() {
        throw unsupportedOperation();
    }

    public EntityDimensions getDimensions(EntityPose pose) {
        throw unsupportedOperation();
    }

    public Vec3d getPos() {
        throw unsupportedOperation();
    }

    @Override
    public BlockPos getBlockPos() {
        throw unsupportedOperation();
    }

    public BlockState getBlockStateAtPos() {
        throw unsupportedOperation();
    }

    public BlockPos getCameraBlockPos() {
        throw unsupportedOperation();
    }

    public ChunkPos getChunkPos() {
        throw unsupportedOperation();
    }

    public Vec3d getVelocity() {
        throw unsupportedOperation();
    }

    public void setVelocity(Vec3d velocity) {
        throw unsupportedOperation();
    }

    public void setVelocity(double x, double y, double z) {
        throw unsupportedOperation();
    }

    public double offsetX(double widthScale) {
        throw unsupportedOperation();
    }

    public double getParticleX(double widthScale) {
        throw unsupportedOperation();
    }

    public double getBodyY(double heightScale) {
        throw unsupportedOperation();
    }

    public double getRandomBodyY() {
        throw unsupportedOperation();
    }

    public double getEyeY() {
        throw unsupportedOperation();
    }

    public double offsetZ(double widthScale) {
        throw unsupportedOperation();
    }

    public double getParticleZ(double widthScale) {
        throw unsupportedOperation();
    }

    public void checkDespawn() {
        throw unsupportedOperation();
    }

    public Vec3d getLeashPos(float delta) {
        throw unsupportedOperation();
    }

    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        throw unsupportedOperation();
    }

    @Nullable
    public ItemStack getPickBlockStack() {
        throw unsupportedOperation();
    }

    public void setInPowderSnow(boolean inPowderSnow) {
        throw unsupportedOperation();
    }

    public boolean canFreeze() {
        throw unsupportedOperation();
    }

    public boolean shouldEscapePowderSnow() {
        throw unsupportedOperation();
    }

    public float getYaw() {
        throw unsupportedOperation();
    }

    public void setYaw(float yaw) {
        throw unsupportedOperation();
    }

    public float getPitch() {
        throw unsupportedOperation();
    }

    public void setPitch(float pitch) {
        throw unsupportedOperation();
    }

    @Nullable
    public net.minecraft.entity.Entity.RemovalReason getRemovalReason() {
        throw unsupportedOperation();
    }

    protected void unsetRemoved() {
        throw unsupportedOperation();
    }

    @Override
    public void setChangeListener(EntityChangeListener changeListener) {
        throw unsupportedOperation();
    }

    @Override
    public boolean shouldSave() {
        throw unsupportedOperation();
    }

    @Override
    public boolean isPlayer() {
        throw unsupportedOperation();
    }

    public boolean canModifyAt(World world, BlockPos pos) {
        throw unsupportedOperation();
    }

    public World getWorld() {
        throw unsupportedOperation();
    }

}
