package codeOrchestra.colt.core.ui.components.inputFormsNew

import codeOrchestra.colt.core.ui.components.inputFormsNew.base.InputForm
import codeOrchestra.groovyfx.FXBindable
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.control.CheckBox

/**
 * @author Dima Kruk
 */
class CInputForm extends InputForm {
    @FXBindable boolean selected

    protected final CheckBox checkBox = new CheckBox()

    CInputForm() {
        setLeftAnchor(checkBox, 10)
        setRightAnchor(checkBox, 10)

        checkBox.textProperty().bindBidirectional(title())
        checkBox.selectedProperty().bindBidirectional(selected())

        textField.disableProperty().bind(selected().not())


        children.add(checkBox)

    }

    @Override
    void activateValidation() {
        super.activateValidation()
        selected().addListener({ ObservableValue<? extends Boolean> observableValue, Boolean t, Boolean newValue ->
            if(newValue) {
                validateValue()
            } else {
                error = false
            }
        } as ChangeListener)
    }
}