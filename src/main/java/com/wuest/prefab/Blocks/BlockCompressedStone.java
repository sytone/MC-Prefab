package com.wuest.prefab.Blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.wuest.prefab.ModRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Provides a way to store large amounts of stone.
 * 
 * @author WuestMan
 */
public class BlockCompressedStone extends Block
{
	public final EnumType typeofStone;
	
	/**
	 * Initializes a new instance of the CompressedStone class.
	 */
	public BlockCompressedStone(EnumType typeOfStone)
	{
		super(Material.ROCK);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
		this.setHarvestLevel(null, 0);
		this.setSoundType(SoundType.STONE);
		ModRegistry.setBlockName(this, typeOfStone.unlocalizedName);
		this.typeofStone = typeOfStone;
	}

	/**
	 * Get a light value for the block at the specified coordinates, normal ranges are between 0 and 15
	 *
	 * @param state Block state
	 * @param world The current world
	 * @param pos Block position in world
	 * @return The light value
	 */
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		IBlockState other = world.getBlockState(pos);

		if (other.getBlock() != this)
		{
			return other.getLightValue(world, pos);
		}

		EnumType meta = this.typeofStone;

		if (meta == EnumType.COMPRESSED_GLOWSTONE || meta == EnumType.DOUBLE_COMPRESSED_GLOWSTONE)
		{
			return 15;
		}

		return state.getLightValue();
	}

	/**
	 * Determines if the player can harvest this block, obtaining it's drops when the block is destroyed.
	 *
	 * @param world The world where the block resides.
	 * @param pos The block position.
	 * @param player The player damaging the block, may be null.
	 * @return True to spawn the drops.
	 */
	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	/**
	 * An enum which contains the various types of block variants.
	 * 
	 * @author WuestMan
	 *
	 */
	public static enum EnumType implements IStringSerializable
	{
		COMPRESSED_STONE(0, "block_compressed_stone", "block_compressed_stone"),
		DOUBLE_COMPRESSED_STONE(1, "block_double_compressed_stone", "block_double_compressed_stone"),
		TRIPLE_COMPRESSED_STONE(2, "block_triple_compressed_stone", "block_triple_compressed_stone"),
		COMPRESSED_GLOWSTONE(3, "block_compressed_glowstone", "block_compressed_glowstone"),
		DOUBLE_COMPRESSED_GLOWSTONE(4, "block_double_compressed_glowstone", "block_double_compressed_glowstone"),
		COMPRESSED_DIRT(5, "block_compressed_dirt", "block_compressed_dirt"),
		DOUBLE_COMPRESSED_DIRT(6, "block_double_compressed_dirt", "block_double_compressed_dirt");

		private final int meta;

		/** The EnumType's name. */
		private final String name;
		private final String unlocalizedName;
		/** Array of the Block's BlockStates */
		private static final BlockCompressedStone.EnumType[] META_LOOKUP = new BlockCompressedStone.EnumType[values().length];

		private EnumType(int meta, String name)
		{
			this(meta, name, name);
		}

		private EnumType(int meta, String name, String unlocalizedName)
		{
			this.meta = meta;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		/**
		 * A list of resource locations for the names.
		 * 
		 * @return A list of resource locations for the numerous types in this enum.
		 */
		public static ResourceLocation[] GetNames()
		{
			List<ResourceLocation> list = Lists.newArrayList();

			for (EnumType type : EnumType.values())
			{
				list.add(new ResourceLocation("prefab", type.unlocalizedName));
			}

			return list.toArray(new ResourceLocation[list.size()]);
		}

		/**
		 * The EnumType's meta data value.
		 * 
		 * @return the meta data for this block.
		 */
		public int getMetadata()
		{
			return this.meta;
		}

		/**
		 * Gets the name of this enum value.
		 */
		public String toString()
		{
			return this.name;
		}

		@Override
		public String getName()
		{
			return this.name;
		}

		/**
		 * The unlocalized name of this EnumType.
		 * 
		 * @return A string containing the unlocalized name.
		 */
		public String getUnlocalizedName()
		{
			return this.unlocalizedName;
		}

		/**
		 * Returns an EnumType for the BlockState from a metadata value.
		 * 
		 * @param meta The meta data value to equate to a {@link BlockCompressedObsidian.EnumType}
		 * @return If the meta data is invalid the default will be used, otherwise the EnumType found.
		 */
		public static BlockCompressedStone.EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length)
			{
				meta = 0;
			}

			return META_LOOKUP[meta];
		}

		static
		{
			for (BlockCompressedStone.EnumType type : values())
			{
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}
