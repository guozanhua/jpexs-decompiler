/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewObjectIns;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class NewObjectAVM2Item extends AVM2Item {

    public List<NameValuePair> pairs;

    public NewObjectAVM2Item(AVM2Instruction instruction, List<NameValuePair> pairs) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.pairs = pairs;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean singleLine = pairs.size() < 2;
        //no new line before as it may break return clause (#593)
        writer.append("{");
        if (!singleLine) {
            writer.newLine();
            writer.indent();
        }
        for (int n = 0; n < pairs.size(); n++) {
            if (n > 0) {
                writer.append(",").newLine();
            }
            pairs.get(n).toString(writer, localData);
        }
        if (!singleLine) {
            writer.newLine();
            writer.unindent();
        }
        writer.append("}");
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Object");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphTargetItem> args = new ArrayList<>();
        for (NameValuePair p : pairs) {
            args.add(p.name);
            args.add(p.value);
        }
        return toSourceMerge(localData, generator, args,
                new AVM2Instruction(0, new NewObjectIns(), new int[]{pairs.size()})
        );
    }
}
