package com.dripps.voxyserver.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

// batch of lod sections sent together for efficiency
public record LODBulkPayload(
        Identifier dimension,
        List<LODSectionPayload> sections
) implements CustomPacketPayload {

    public static final Type<LODBulkPayload> TYPE =
            new Type<>(Identifier.parse("voxyserver:lod_bulk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LODBulkPayload> CODEC =
            StreamCodec.of(LODBulkPayload::write, LODBulkPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, LODBulkPayload payload) {
        buf.writeIdentifier(payload.dimension);
        buf.writeVarInt(payload.sections.size());
        for (LODSectionPayload section : payload.sections) {
            // write each section inline without redundant dimension
            buf.writeLong(section.sectionKey());
            buf.writeVarInt(section.lutBlockStateIds().length);
            for (int i = 0; i < section.lutBlockStateIds().length; i++) {
                buf.writeVarInt(section.lutBlockStateIds()[i]);
                buf.writeVarInt(section.lutBiomeIds()[i]);
                buf.writeByte(section.lutLight()[i]);
            }
            buf.writeVarInt(section.indexArray().length);
            for (short idx : section.indexArray()) {
                buf.writeShort(idx);
            }
        }
    }

    private static LODBulkPayload read(RegistryFriendlyByteBuf buf) {
        Identifier dimension = buf.readIdentifier();
        int count = buf.readVarInt();
        List<LODSectionPayload> sections = new ArrayList<>(count);
        for (int s = 0; s < count; s++) {
            long sectionKey = buf.readLong();
            int lutLen = buf.readVarInt();
            int[] blockStateIds = new int[lutLen];
            int[] biomeIds = new int[lutLen];
            byte[] light = new byte[lutLen];
            for (int i = 0; i < lutLen; i++) {
                blockStateIds[i] = buf.readVarInt();
                biomeIds[i] = buf.readVarInt();
                light[i] = buf.readByte();
            }
            int indexLen = buf.readVarInt();
            short[] indexArray = new short[indexLen];
            for (int i = 0; i < indexLen; i++) {
                indexArray[i] = buf.readShort();
            }
            sections.add(new LODSectionPayload(dimension, sectionKey, blockStateIds, biomeIds, light, indexArray));
        }
        return new LODBulkPayload(dimension, sections);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
