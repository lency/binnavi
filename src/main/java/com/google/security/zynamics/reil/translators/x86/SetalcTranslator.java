/*
Copyright 2015 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.reil.translators.x86;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;


/**
 * Translates SETALC instructions to REIL code.
 */
public class SetalcTranslator implements IInstructionTranslator {
  /**
   * Translates a SETALC instruction to REIL code.
   * 
   * @param environment A valid translation environment
   * @param instruction The SETALC instruction to translate
   * @param instructions The generated REIL code will be added to this list
   * 
   * @throws InternalTranslationException if any of the arguments are null the passed instruction is
   *         not an ADC instruction
   */
  @Override
  public void translate(final ITranslationEnvironment environment, final IInstruction instruction,
      final List<ReilInstruction> instructions) throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, "setalc");

    if (instruction.getOperands().size() != 0) {
      throw new InternalTranslationException(
          "Error: Argument instruction is not an setalc instruction (invalid number of operands)");
    }

    final long baseOffset = instruction.getAddress().toLong() * 0x100;
    long offset = baseOffset;

    final String subResult = environment.getNextVariableString();
    final String truncatedSubResult = environment.getNextVariableString();
    final String clearedEax = environment.getNextVariableString();

    // Calculate new value of AL from CF
    instructions.add(ReilHelpers.createSub(offset++, OperandSize.BYTE, "0", OperandSize.BYTE,
        Helpers.CARRY_FLAG, OperandSize.WORD, subResult));
    instructions.add(ReilHelpers.createAnd(offset++, OperandSize.WORD, subResult, OperandSize.BYTE,
        "255", OperandSize.BYTE, truncatedSubResult));

    // Mask new value of AL into EAX
    instructions.add(ReilHelpers.createAnd(offset++, OperandSize.DWORD, "eax", OperandSize.DWORD,
        String.valueOf(0xFFFFFF00), OperandSize.BYTE, clearedEax));
    instructions.add(ReilHelpers.createOr(offset++, OperandSize.DWORD, clearedEax,
        OperandSize.BYTE, truncatedSubResult, OperandSize.DWORD, "eax"));
  }
}
