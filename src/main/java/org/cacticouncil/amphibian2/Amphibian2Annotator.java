package org.cacticouncil.amphibian2;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class Amphibian2Annotator implements Annotator
{
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder)
    {
        // TODO: determine solution to support multiple languages, only supports Java currently
//        String elementDescription = ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE);
//        System.out.println(elementDescription);
//
//        if (elementDescription.contains("method") || elementDescription.contains("function"))
//        {
//            Amphibian2ColorGenerator.colorMethodBlock(element, holder);
//        }

        // Get the color of the block based on the syntax element
        Amphibian2ColorGenerator.getBlockColor(element, holder);
    }
}
