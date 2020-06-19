package org.cacticouncil.amphibian2;

import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import org.jetbrains.annotations.Nullable;

public class Amphibian2EditorColorsListener implements EditorColorsListener
{
    @Override
    public void globalSchemeChange(@Nullable EditorColorsScheme scheme)
    {
        // Update scheme when it changes
        Amphibian2ColorGenerator.schemeChange(scheme);
    }
}
