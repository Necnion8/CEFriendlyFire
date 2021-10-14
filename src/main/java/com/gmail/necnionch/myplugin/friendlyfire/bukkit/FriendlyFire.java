package com.gmail.necnionch.myplugin.friendlyfire.bukkit;

import com.google.common.collect.Sets;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FriendlyFire extends JavaPlugin implements Listener {
    public static final String PERMS_COMMAND = "friendlyfire.command.friendlyfire";
    private final Set<Set<UUID>> groups = Sets.newHashSet();


    @Override
    public void onEnable() {
        new CommandAPICommand("cefriendlyfire")
                .withPermission(PERMS_COMMAND)
                .withSubcommand(new CommandAPICommand("group")
                        .withArguments(new EntitySelectorArgument("targets", EntitySelectorArgument.EntitySelector.MANY_ENTITIES))
                        .executesNative(this::execGroup)
                )
                .withSubcommand(new CommandAPICommand("ungroup")
                        .withArguments(new EntitySelectorArgument("targets", EntitySelectorArgument.EntitySelector.MANY_ENTITIES))
                        .executesNative(this::execUngroup)
                )
                .register();

        getServer().getPluginManager().registerEvents(this, this);
    }



    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();  // 受けた人
        Entity damager = event.getDamager();

        if (isGroupMember(damager.getUniqueId(), entity.getUniqueId())) {
            event.setCancelled(true);

        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Entity) {
                Entity shooter = (Entity) projectile.getShooter();
                if (isGroupMember(entity.getUniqueId(), shooter.getUniqueId())) {
                    event.setCancelled(true);
                }
            }

        } else if (damager instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) damager;
            if (tnt.getSource() != null) {
                if (isGroupMember(entity.getUniqueId(), tnt.getSource().getUniqueId())) {
                    event.setCancelled(true);
                }
            }
        }
    }



    private void execGroup(NativeProxyCommandSender sender, Object[] args) {
        @SuppressWarnings("unchecked") List<Entity> targets = (List<Entity>) args[0];
        addGroup(targets.stream().map(Entity::getUniqueId).collect(Collectors.toList()));
        sender.getCaller().sendMessage(targets.size() + "体のエンティティをグループ化しました。");
    }

    private void execUngroup(NativeProxyCommandSender sender, Object[] args) {
        @SuppressWarnings("unchecked") List<Entity> targets = (List<Entity>) args[0];
        addGroup(targets.stream().map(Entity::getUniqueId).collect(Collectors.toList()));
        sender.getCaller().sendMessage(targets.size() + "体のグループを解除しました。");
    }



    public void addGroup(Collection<UUID> targets) {
        removeGroups(targets);
        groups.add(Sets.newHashSet(targets));
    }

    public void removeGroups(Collection<UUID> targets) {
        groups.forEach(members -> members.removeAll(targets));
        groups.removeIf(Set::isEmpty);
    }


    public boolean isGroupMember(UUID target1, UUID target2) {
        for (Set<UUID> members : groups) {
            if (members.contains(target1) && members.contains(target2))
                return true;
        }
        return false;
    }

}
