package com.dripps.voxyserver.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// single level 0 LOD section sent from server to client
// contains: dimension id, section position key, a LUT of vanilla registry ids, and an index array
public record LODSectionPayload(
        Identifier dimension,
        long sectionKey,
        int[] lutBlockStateIds,
        int[] lutBiomeIds,
        byte[] lutLight,
        short[] indexArray
) implements CustomPacketPayload {

    public static final Type<LODSectionPayload> TYPE =
            new Type<>(Identifier.parse("voxyserver:lod_section"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LODSectionPayload> CODEC =
            StreamCodec.of(LODSectionPayload::write, LODSectionPayload::read);

    private static void write(RegistryFriendlyByteBuf buf, LODSectionPayload payload) {
        buf.writeIdentifier(payload.dimension);
        buf.writeLong(payload.sectionKey);

        buf.writeVarInt(payload.lutBlockStateIds.length);
        for (int i = 0; i < payload.lutBlockStateIds.length; i++) {
            buf.writeVarInt(payload.lutBlockStateIds[i]);
            buf.writeVarInt(payload.lutBiomeIds[i]);
            buf.writeByte(payload.lutLight[i]);
        }

        buf.writeVarInt(payload.indexArray.length);
        for (short idx : payload.indexArray) {
            buf.writeShort(idx);
        }
    }

    private static LODSectionPayload read(RegistryFriendlyByteBuf buf) {
        Identifier dimension = buf.readIdentifier();
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

        return new LODSectionPayload(dimension, sectionKey, blockStateIds, biomeIds, light, indexArray);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
