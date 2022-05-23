package nova.committee.avaritia.common.item.singularity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;
import nova.committee.avaritia.init.config.ModConfig;
import nova.committee.avaritia.util.lang.Localizable;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/4/2 12:34
 * Version: 1.0
 */
public class Singularity {
    private final ResourceLocation id;
    private final String name;
    private final int[] colors;
    private final String tag;
    private final int ingredientCount;
    private final int timeRequired;
    private Ingredient ingredient;
    private boolean enabled = true;

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient, int ingredientCount, int timeRequired) {
        this.id = id;
        this.name = name;
        this.colors = colors;
        this.ingredient = ingredient;
        this.tag = null;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, Ingredient ingredient) {
        this(id, name, colors, ingredient, -1, ModConfig.SERVER.singularityTimeRequired.get());
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag, int ingredientCount, int timeRequired) {
        this.id = id;
        this.name = name;
        this.colors = colors;
        this.ingredient = Ingredient.EMPTY;
        this.tag = tag;
        this.ingredientCount = ingredientCount;
        this.timeRequired = timeRequired;
    }

    public Singularity(ResourceLocation id, String name, int[] colors, String tag) {
        this(id, name, colors, tag, -1, ModConfig.SERVER.singularityTimeRequired.get());
    }

    public static Singularity read(FriendlyByteBuf buffer) {
        var id = buffer.readResourceLocation();
        var name = buffer.readUtf();
        int[] colors = buffer.readVarIntArray();
        var isTagIngredient = buffer.readBoolean();
        int time = buffer.readVarInt();

        String tag = null;
        var ingredient = Ingredient.EMPTY;

        if (isTagIngredient) {
            tag = buffer.readUtf();
        } else {
            ingredient = Ingredient.fromNetwork(buffer);
        }

        int ingredientCount = buffer.readVarInt();

        Singularity singularity;
        if (isTagIngredient) {
            singularity = new Singularity(id, name, colors, tag, ingredientCount, time);
        } else {
            singularity = new Singularity(id, name, colors, ingredient, ingredientCount, time);
        }

        singularity.enabled = buffer.readBoolean();

        return singularity;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getOverlayColor() {
        return this.colors[0];
    }

    public int getUnderlayColor() {
        return this.colors[1];
    }

    public String getTag() {
        return this.tag;
    }

    public Ingredient getIngredient() {
        if (this.tag != null && this.ingredient == Ingredient.EMPTY) {
            var tag = ItemTags.create(new ResourceLocation(this.tag));
            this.ingredient = Ingredient.of(tag);
        }

        return this.ingredient;
    }

    public int getIngredientCount() {
        if (this.ingredientCount == -1) {
            return 1000;
        }
        return this.ingredientCount;
    }

    public Component getDisplayName() {
        return Localizable.of(this.name).build();
    }

    public int getTimeRequired() {
        return timeRequired;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(this.id);
        buffer.writeUtf(this.name);
        buffer.writeVarIntArray(this.colors);
        buffer.writeBoolean(this.tag != null);

        if (this.tag != null) {
            buffer.writeUtf(this.tag);
        } else {
            this.ingredient.toNetwork(buffer);
        }

        buffer.writeVarInt(this.getIngredientCount());
        buffer.writeBoolean(this.enabled);
    }
}
