/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.avm2.parser.MissingSymbolHandler;
import javax.swing.JOptionPane;
import static com.jpexs.decompiler.flash.gui.AppStrings.translate;

public class DialogMissingSymbolHandler implements MissingSymbolHandler {

    @Override
    public boolean missingString(String value) {
        return JOptionPane.showConfirmDialog(null, translate("message.constant.new.string").replace("%value%", value), translate("message.constant.new.string.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean missingInt(long value) {
        return JOptionPane.showConfirmDialog(null, translate("message.constant.new.integer").replace("%value%", "" + value), translate("message.constant.new.integer.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean missingUInt(long value) {
        return JOptionPane.showConfirmDialog(null, translate("message.constant.new.unsignedinteger").replace("%value%", "" + value), translate("message.constant.new.unsignedinteger.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean missingDouble(double value) {
        return JOptionPane.showConfirmDialog(null, translate("message.constant.new.double").replace("%value%", "" + value), translate("message.constant.new.double.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION;
    }
}