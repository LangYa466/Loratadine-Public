package shop.xmz.lol.loratadine.modules;

import cn.lzq.injection.leaked.invoked.KeyEvent;
import org.lwjgl.glfw.GLFW;
import shop.xmz.lol.loratadine.Loratadine;
import shop.xmz.lol.loratadine.event.annotations.EventTarget;
import shop.xmz.lol.loratadine.modules.impl.combat.*;
import shop.xmz.lol.loratadine.modules.impl.hud.*;
import shop.xmz.lol.loratadine.modules.impl.misc.*;
import shop.xmz.lol.loratadine.modules.impl.movement.*;
import shop.xmz.lol.loratadine.modules.impl.player.*;
import shop.xmz.lol.loratadine.modules.impl.render.*;
import shop.xmz.lol.loratadine.modules.impl.setting.*;
import shop.xmz.lol.loratadine.utils.wrapper.Wrapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleManager implements Wrapper {
    public final Map<Class<? extends Module>, Module> modules = new HashMap<>();
    private final Map<Category, List<Module>> categoryModules = new HashMap<>();

    public ModuleManager() {
        Loratadine.INSTANCE.getEventManager().register(this);
    }

    public void init() {
        // Targets
        registerModules(
                //new Player(),
                new Target()
                //new Animals(),
                //new Invisible(),
                //new Dead()
        );

        // Combat
        registerModules(
                new AimBot(),
                new AutoClicker(),
                new AutoRod(),
                new AutoWeapon(),
                new BackTrack(),
                new KillAura(),
                new SuperKnockBack(),
                new Velocity()
        );

        // Misc
        registerModules(
                new AntiBot(),
                new Insults(),
                new ModuleHelper(),
                new Disabler(),
                new Tracker(),
                new Teams(),
                new Theme()
        );

        // Movement
        registerModules(
                new AntiAim(),
                new MoveFix(),
                new Eagle(),
                new InvMove(),
                new Sneak(),
                new Speed(),
                new Sprint()
        );

        // Player
        registerModules(
                new AntiVoid(),
                new AutoTool(),
                new Blink(),
                new Breaker(),
                new ChestStealer(),
                new FastBreak(),
                new FastPlace(),
                new InvCleaner(),
                new IQBoost(),
                new NoClickDelay(),
                new NoFall(),
                new Reach(),
                new Scaffold()
        );

        // Render
        registerModules(
                new Ambience(),
                new Animations(),
                new BlockESP(),
                new BetterCamera(),
                new ClickGui(),
                new Effects(),
                new ESP(),
                new FullBright(),
                new HUD(),
                new ItemPhysic(),
                new ModuleList(),
                new NameTags(),
                new NotificationHUD(),
                new SpectrumBar(),
                new TabGui(),
                new TargetHUD(),
                new TargetMarker()
        );
    }

    private void registerModules(Module ... modules) {
        List.of(modules).forEach(this::registerModule);
    }

    private void registerModule(Module module) {
        modules.put(module.getClass(), module);
    }

    public Module findModule(String name) {
        for (Module module : getModules()) {
            if (module.getName().replace(" ", "").equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public Module getModule(Class<? extends Module> moduleClazz) {
        return this.modules.get(moduleClazz);
    }

    public List<Module> getModule(Category category) {
        return modules.values().stream()
                .filter(module -> module.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public Collection<Module> getModules() {
        return modules.values();
    }

    public List<Module> getModulesByCategory(Category category) {
        final List<Module> tryGet = categoryModules.get(category);
        if (tryGet == null) {
            final List<Module> modules1 = modules.values().stream().filter(module -> module.getCategory() == category).toList();
            categoryModules.put(category, modules1);
            return modules1;
        }
        return tryGet;
    }

    @EventTarget
    public void onKey(KeyEvent event) {
        if ((mc != null && mc.player != null && mc.level != null) && mc.screen == null) {
            for (Module module : getModules()) {
                if (module.getKey() != GLFW.GLFW_KEY_UNKNOWN
                        && module.getKey() == event.getKeyCode()
                        && mc.screen == null
                        || (module.getKey() == GLFW.GLFW_KEY_RIGHT_SHIFT && event.getKeyCode() == GLFW.GLFW_KEY_UNKNOWN && mc.screen == null)) /*ClickGUI RShift (Minecraft shit bug)*/ {
                    module.toggle();
                }
            }
        }
    }
}
