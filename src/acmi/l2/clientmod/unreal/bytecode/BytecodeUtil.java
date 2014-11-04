package acmi.l2.clientmod.unreal.bytecode;

import acmi.l2.clientmod.io.DataInput;
import acmi.l2.clientmod.io.DataOutput;
import acmi.l2.clientmod.unreal.UnrealException;
import acmi.l2.clientmod.unreal.bytecode.token.Token;

import java.util.List;

public interface BytecodeUtil {
    List<Token> readTokens(DataInput input, int scriptSize) throws UnrealException;

    int writeTokens(DataOutput output, List<Token> tokens) throws UnrealException;
}
