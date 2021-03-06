/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.nmplayer.ui;

import com.djrapitops.nmplayer.messaging.MessageSender;
import javafx.scene.control.TextArea;

/**
 * JavaFx UI component, a Disabled TextArea.
 *
 * @author Rsl1122
 * @see TextArea
 * @see MessageSender
 */
public class TextConsole extends TextArea {

    /**
     * Constructor for the component.
     * <p>
     * Disables the TextArea, but makes it appear editable.
     * <p>
     * Sets itself as the output of MessageSender.
     *
     * @see MessageSender
     */
    public TextConsole() {
        super();
        super.setWrapText(true);
        super.setEditable(false);
        super.setPrefWidth(10000);
        super.setMouseTransparent(true);
        super.setFocusTraversable(false);
        super.maxHeight(3);
    }

}
