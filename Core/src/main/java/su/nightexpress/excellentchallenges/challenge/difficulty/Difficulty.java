package su.nightexpress.excellentchallenges.challenge.difficulty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.wrapper.UniInt;

import java.util.HashMap;
import java.util.Map;

public class Difficulty {

    public static final String DEF_CHILD   = "childish";
    public static final String DEF_EASY    = "easy";
    public static final String DEF_MEDIUM  = "medium";
    public static final String DEF_HARD    = "hard";
    public static final String DEF_EXTREME = "extreme";

    private final String id;
    private final String name;
    private final UniInt levels;
    private final Map<String, DifficultyModifier> modifiers;

    public Difficulty(@NotNull String id, @NotNull String name,
                      @NotNull UniInt levels,
                      @NotNull Map<String, DifficultyModifier> modifiers) {
        this.id = id.toLowerCase();
        this.name = name;
        this.levels = levels;
        this.modifiers = modifiers;
    }

    @NotNull
    public static Difficulty read(@NotNull FileConfig cfg, @NotNull String path, @NotNull String id) {
        String name = cfg.getString(path + ".Name", StringUtil.capitalizeUnderscored(id));
        UniInt levels = UniInt.read(cfg, path + ".Levels");
        Map<String, DifficultyModifier> modifiers = new HashMap<>();
        for (String modId : cfg.getSection(path + ".Modifiers")) {
            DifficultyModifier modifier = DifficultyModifier.read(cfg, path + ".Modifiers." + modId);
            modifiers.put(modId.toLowerCase(), modifier);
        }

        return new Difficulty(id, name, levels, modifiers);
    }

    public void write(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.set(path + ".Name", this.getName());
        this.getLevels().write(cfg, path + ".Levels");

        cfg.remove(path + ".Modifiers");
        this.getModifiers().forEach((id, modifier) -> modifier.write(cfg, path + ".Modifiers." + id));
    }

    public int createLevel() {
        return this.getLevels().roll();
    }

    @Nullable
    public DifficultyModifier getModifier(@NotNull String id) {
        return this.getModifiers().get(id.toLowerCase());
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public UniInt getLevels() {
        return levels;
    }

    @NotNull
    public Map<String, DifficultyModifier> getModifiers() {
        return modifiers;
    }
}
