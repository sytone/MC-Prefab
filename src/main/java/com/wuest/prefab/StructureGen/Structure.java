package com.wuest.prefab.StructureGen;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.gson.Gson;
import com.wuest.prefab.BuildingMethods;
import com.wuest.prefab.Prefab;
import com.wuest.prefab.Config.StructureConfiguration;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Each structure represents a building which is pre-defined in a JSON file.
 * 
 * @author WuestMan
 */
public class Structure
{
	private String name;
	private BuildClear clearSpace;
	private ArrayList<BuildFloor> floors;
	private ArrayList<BuildWall> walls;
	private ArrayList<BuildBlock> blocks;
	protected ArrayList<BuildBlock> placedBlocks;

	public Structure()
	{
		this.Initialize();
	}

	/**
	 * Creates an instance of the structure after reading from a resource
	 * location and converting it from JSON.
	 * 
	 * @param resourceLocation The location of the JSON file to load. Example:
	 *            "assets/prefab/structures/warehouse.json"
	 * @return 
	 * @return Null if the resource wasn't found or the JSON could not be
	 *         parsed, otherwise the de-serialized object.
	 */
	public static <T extends Structure> T CreateInstance(String resourceLocation, Class<? extends Structure> child)
	{
		T structure = null;

		try
		{
			Gson file = new Gson();
			InputStreamReader reader = new InputStreamReader(Prefab.class.getClassLoader().getResourceAsStream(resourceLocation), "UTF-8");

			if (reader != null)
			{
				
				structure = (T) file.fromJson(reader, child);
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return structure;
	}

	public static void CreateStructureFile(Structure structure, String fileLocation)
	{
		try
		{
			Gson converter = new Gson();
			FileWriter writer = new FileWriter(fileLocation);
			converter.toJson(structure, writer);

			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void ScanStructure(World world, BlockPos originalPos, BlockPos cornerPos1, BlockPos cornerPos2, String fileLocation)
	{
		Structure scannedStructure = new Structure();
		scannedStructure.getClearSpace().getShape().setDirection(EnumFacing.SOUTH);
		scannedStructure.getClearSpace().getShape().setHeight(17);
		scannedStructure.getClearSpace().getShape().setLength(17);
		scannedStructure.getClearSpace().getShape().setWidth(17);
		scannedStructure.getClearSpace().getStartingPosition().setEastOffset(7);
		scannedStructure.getClearSpace().getStartingPosition().setHeightOffset(-7);
		scannedStructure.getClearSpace().getStartingPosition().setSouthOffset(1);

		for (BlockPos currentPos : BlockPos.getAllInBox(cornerPos1, cornerPos2))
		{
			if (world.isAirBlock(currentPos))
			{
				continue;
			}

			IBlockState currentState = world.getBlockState(currentPos);
			Block currentBlock = currentState.getBlock();

			BuildBlock buildBlock = new BuildBlock();
			buildBlock.setBlockDomain(currentBlock.getRegistryName().getResourceDomain());
			buildBlock.setBlockName(currentBlock.getRegistryName().getResourcePath());

			// if (currentPos.getX() > hitBlockPos.getX()). currentPos is "East"
			// of hitBlock
			// if (currentPos.getZ() > hitBlockPos.getZ()). currentPos is
			// "South" of hitBlock

			if (currentPos.getX() > originalPos.getX())
			{
				buildBlock.getStartingPosition().setEastOffset(currentPos.getX() - originalPos.getX());
			}
			else
			{
				buildBlock.getStartingPosition().setWestOffset(originalPos.getX() - currentPos.getX());
			}

			if (currentPos.getZ() > originalPos.getZ())
			{
				buildBlock.getStartingPosition().setSouthOffset(currentPos.getZ() - originalPos.getZ());
			}
			else
			{
				buildBlock.getStartingPosition().setNorthOffset(originalPos.getZ() - currentPos.getZ());
			}

			buildBlock.getStartingPosition().setHeightOffset(currentPos.getY() - originalPos.getY());

			ImmutableMap<IProperty<?>, Comparable<?>> properties = currentState.getProperties();

			for (Entry<IProperty<?>, Comparable<?>> entry : properties.entrySet())
			{
				BuildProperty property = new BuildProperty();
				property.setName(entry.getKey().getName());
				property.setValue(entry.getValue().toString());

				buildBlock.getProperties().add(property);
			}

			scannedStructure.getBlocks().add(buildBlock);
		}

		Structure.CreateStructureFile(scannedStructure, fileLocation);
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String value)
	{
		this.name = value;
	}

	public BuildClear getClearSpace()
	{
		return this.clearSpace;
	}

	public void setClearSpace(BuildClear value)
	{
		this.clearSpace = value;
	}

	public ArrayList<BuildFloor> getFloors()
	{
		return this.floors;
	}

	public void setFloors(ArrayList<BuildFloor> value)
	{
		this.floors = value;
	}

	public ArrayList<BuildWall> getWalls()
	{
		return this.walls;
	}

	public void setWalls(ArrayList<BuildWall> value)
	{
		this.walls = value;
	}

	public ArrayList<BuildBlock> getBlocks()
	{
		return this.blocks;
	}

	public void setBlocks(ArrayList<BuildBlock> value)
	{
		this.blocks = value;
	}

	public void Initialize()
	{
		this.name = "";
		this.clearSpace = new BuildClear();
		this.floors = new ArrayList<BuildFloor>();
		this.walls = new ArrayList<BuildWall>();
		this.blocks = new ArrayList<BuildBlock>();
	}

	/**
	 * This is the main building method for this structure.
	 * 
	 * @param configuration The configuration the user updated.
	 * @param world The current world.
	 * @param originalPos The block the user clicked on.
	 * @param assumedNorth The assumed "north". All block orientations are based
	 *            off of this assumed "north".
	 */
	public void BuildStructure(StructureConfiguration configuration, World world, BlockPos originalPos, EnumFacing assumedNorth)
	{
		// First, clear the area where the structure will be built.
		this.ClearSpace(configuration, world, originalPos, assumedNorth);

		this.placedBlocks = new ArrayList<BuildBlock>();
		
		// Now place all of the blocks.
		for (BuildBlock block : this.getBlocks())
		{
			Block foundBlock = Block.REGISTRY.getObject(block.getResourceLocation());

			if (foundBlock != null)
			{
				IBlockState blockState = foundBlock.getDefaultState();
				BuildBlock subBlock = null;
				
				if (!this.CustomBlockProcessingHandled(configuration, block, world, originalPos, assumedNorth, foundBlock, blockState))
				{
					block = BuildBlock.SetBlockState(configuration, world, originalPos, assumedNorth, block, foundBlock, blockState);
					
					if (block.getSubBlock() != null)
					{
						foundBlock = Block.REGISTRY.getObject(block.getSubBlock().getResourceLocation());
						blockState = foundBlock.getDefaultState();
						
						subBlock = BuildBlock.SetBlockState(configuration, world, originalPos, assumedNorth, block.getSubBlock(), foundBlock, blockState);
					}
					
					if (!block.getHasFacing())
					{
						BuildingMethods.ReplaceBlock(world, block.getStartingPosition().getRelativePosition(originalPos, configuration.houseFacing), block.getBlockState());
						
						if (subBlock != null)
						{
							BuildingMethods.ReplaceBlock(world, subBlock.getStartingPosition().getRelativePosition(originalPos, configuration.houseFacing), subBlock.getBlockState());
						}
					}
					else
					{
						if (subBlock != null)
						{
							block.setSubBlock(subBlock);
						}
						
						this.placedBlocks.add(block);
					}
				}
			}
		}
		
		// Now place all of the facing blocks. This needs to be done here these blocks may not "stick" before all of the other solid blocks are placed.
		for (BuildBlock currentBlock : this.placedBlocks)
		{
			BuildingMethods.ReplaceBlock(world, currentBlock.getStartingPosition().getRelativePosition(originalPos, configuration.houseFacing), currentBlock.getBlockState());
			
			// After placing the initial block, set the sub-block. This needs to happen as the list isn't always in the correct order.
			if (currentBlock.getSubBlock() != null)
			{
				BuildBlock subBlock = currentBlock.getSubBlock();
				
				BuildingMethods.ReplaceBlock(world, subBlock.getStartingPosition().getRelativePosition(originalPos, configuration.houseFacing), subBlock.getBlockState());
			}
		}
	}

	protected void ClearSpace(StructureConfiguration configuration, World world, BlockPos originalPos, EnumFacing assumedNorth)
	{
		BuildingMethods.ClearSpaceExact(world, this.clearSpace.getStartingPosition().getRelativePosition(originalPos, configuration.houseFacing),
				this.clearSpace.getShape().getWidth(), this.clearSpace.getShape().getHeight(), this.clearSpace.getShape().getLength(), configuration.houseFacing);
	}

	protected Boolean CustomBlockProcessingHandled(StructureConfiguration configuration, BuildBlock block, World world, BlockPos originalPos, EnumFacing assumedNorth,
			Block foundBlock, IBlockState blockState)
	{
		return false;
	}
}