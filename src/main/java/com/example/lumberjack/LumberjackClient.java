package com.example.lumberjack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

/** Клиентский мод: рывок считается у вас на компьютере, на сервер ничего ставить не нужно. */
public class LumberjackClient implements ClientModInitializer {

    /** Имя, которое нужно дать золотому топору в наковальне */
    public static final String AXE_NAME = "Нерушимый топор дровосека";
    /** Сила рывка (чем больше, тем дальше летим) */
    private static final double DASH_POWER = 1.8;
    /** Перезарядка в тиках (20 тиков = 1 секунда) */
    private static final int COOLDOWN_TICKS = 20;

    private static int lastDashAge = -1000;

    /** Это золотой топор с нужным именем? */
    public static boolean isDashAxe(ItemStack stack) {
        if (!stack.isOf(Items.GOLDEN_AXE)) return false;
        Text name = stack.get(DataComponentTypes.CUSTOM_NAME);
        return name != null && name.getString().trim().equalsIgnoreCase(AXE_NAME);
    }

    @Override
    public void onInitializeClient() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            // Это событие вызывается и на сервере (в одиночной игре) - нам нужен только клиент
            if (!world.isClient()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);
            if (!isDashAxe(stack)) return ActionResult.PASS;
            // diff < 0 бывает после входа в другой мир (age начинается заново) - тогда сбрасываем перезарядку
            int diff = player.age - lastDashAge;
            if (diff >= 0 && diff < COOLDOWN_TICKS) return ActionResult.PASS;
            lastDashAge = player.age;

            Vec3d dir = player.getRotationVec(1.0f).normalize().multiply(DASH_POWER);
            player.setVelocity(dir);
            player.fallDistance = 0;
            player.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.2f);

            // PASS: дальше всё работает как у обычного топора
            return ActionResult.PASS;
        });
    }
}
