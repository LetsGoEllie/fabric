/*
 * Copyright (c) 2016, 2017, 2018 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.registry.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.registry.ListenableRegistry;
import net.fabricmc.fabric.registry.RegistryListener;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.item.Item;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(ParticleManager.class)
public class MixinParticleManager implements RegistryListener<ParticleType> {
	@Shadow
	private Int2ObjectMap<ParticleFactory<?>> factories;

	private Map<Identifier, ParticleFactory<?>> fabricFactoryMap;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onInit(World world, TextureManager textureManager, CallbackInfo info) {
		((ListenableRegistry<ParticleType>) Registry.PARTICLE_TYPE).registerListener(this);
	}

	@Override
	public void beforeRegistryCleared(Registry<ParticleType> registry) {
		if (fabricFactoryMap == null) {
			fabricFactoryMap = new HashMap<>();
		}

		for (Identifier id : registry.keys()) {
			ParticleType object = registry.get(id);
			int rawId = registry.getRawId(object);
			ParticleFactory<?> factory = factories.get(rawId);
			if (factory != null) {
				fabricFactoryMap.put(id, factory);
			}
		}

		factories.clear();
	}

	@Override
	public void beforeRegistryRegistration(Registry<ParticleType> registry, int id, Identifier identifier, ParticleType object, boolean isNew) {
		if (fabricFactoryMap != null && fabricFactoryMap.containsKey(identifier)) {
			factories.put(id, fabricFactoryMap.get(identifier));
		}
	}
}
