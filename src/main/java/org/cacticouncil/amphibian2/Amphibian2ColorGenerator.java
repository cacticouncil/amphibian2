package org.cacticouncil.amphibian2;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

public class Amphibian2ColorGenerator
{
    // TA Keys
    private static final TextAttributesKey importKey    = TextAttributesKey.createTextAttributesKey("IMPORT_KEY");
    private static final TextAttributesKey methodKey    = TextAttributesKey.createTextAttributesKey("METHOD_KEY");
    private static final TextAttributesKey statementKey = TextAttributesKey.createTextAttributesKey("STMT_KEY");
    private static final TextAttributesKey classKey     = TextAttributesKey.createTextAttributesKey("CLASS_KEY");
    private static final TextAttributesKey conditionKey = TextAttributesKey.createTextAttributesKey("COND_KEY");

    // Color constants
    private static final Color importColor    = new Color(0x33F6F8F7, true);
    private static final Color methodColor    = new Color(0x33E59B05, true);
    private static final Color statementColor = new Color(0x336FD2E5, true);
    private static final Color classColor     = new Color(0x33A861E0, true);
    private static final Color conditionColor = new Color(0x33E06185, true);

    public static void schemeChange(EditorColorsScheme scheme)
    {
        // TODO: update with all text attribute keys
        // On a scheme change, update with the annotations
        scheme = EditorColorsManager.getInstance().getGlobalScheme();

        TextAttributes ta = scheme.getAttributes(importKey);
        ta.setBackgroundColor(importColor);

        scheme.setAttributes(importKey, ta);
    }

    public static void getBlockColor(@NotNull PsiElement element, @NotNull AnnotationHolder holder)
    {
        // For each element of the code, get the correct block color
        if (element instanceof PsiImportStatement)
        {
            // Get the import color
            getImportBlockColor(element, holder);
        }
        else if (element instanceof PsiClass)
        {
            // Get the class color
            getClassBlockColor((PsiClass) element, holder);
        }
        else if (element instanceof PsiMethod)
        {
            // Get the method color
            getMethodBlockColor((PsiMethod) element, holder);
        }
        else if (element instanceof PsiConditionalLoopStatement)
        {
            // Get the condition color for the generic conditions for loops
            getConditionBlockColor((PsiConditionalLoopStatement) element, holder);

            if (element instanceof PsiWhileStatement)
            {
                // Get the condition color specific to while loops
                getWhileBlockColor((PsiWhileStatement) element, holder);
            }
            else if (element instanceof PsiDoWhileStatement)
            {
                // Get the condition color specific to do while loops
                getDoWhileBlockColor((PsiDoWhileStatement) element, holder);
            }
            else if (element instanceof PsiForStatement)
            {
                // Get the condition color specific to for loops
                getForBlockColor((PsiForStatement) element, holder);
            }
        }
        else if (element instanceof PsiIfStatement)
        {
            // Get the condition color specific to if statements
            getIfBlockColor((PsiIfStatement) element, holder);
        }
        else if (element instanceof  PsiTryStatement)
        {
            // Get the condition color specific to try catch blocks
            getTryBlockColor((PsiTryStatement) element, holder);
        }
        else if (element instanceof PsiDeclarationStatement || element instanceof PsiExpressionStatement
            || element instanceof PsiReturnStatement || element instanceof PsiBreakStatement || element instanceof PsiContinueStatement)
        {
            // Get generic statement color for declaration, expression, and keyword (i.e. break, return) statements
            getStatementBlockColor(element, holder);
        }
    }

    private static void createAnnotation(@NotNull AnnotationHolder holder, TextRange range, Color color)
    {
        // Get the global color scheme
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();

        // Get the default background color of the scheme
        Color bgColor = scheme.getDefaultBackground();

        // Convert the two colors to an array of size 4 each, with each index representing an R, G, B, or A value
        float[] bgArray = bgColor.getRGBComponents(null);
        float[] colorArray = color.getRGBComponents(null);

        // Alpha value to use is the alpha value of the color passed in
        float alpha = colorArray[3];

        // Create an new RGBA array for the result of blending the background with the passed in color together
        float[] newColor = new float[4];

        // Iterate over each RGB component of the colors
        for (int i = 0 ; i < newColor.length; i++)
        {
            // Calculate the blend of the two colors including the alpha value
            newColor[i] = colorArray[i] * alpha + bgArray[i] * (1 - alpha);
        }

        // Create an annotation given the range and blended color
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .enforcedTextAttributes(new TextAttributes(null, new Color(newColor[0], newColor[1], newColor[2]),
                        null, EffectType.BOXED, Font.PLAIN))
                .create();
    }

    private static void getImportBlockColor(@NotNull PsiElement element, @NotNull AnnotationHolder holder)
    {
        // Get the range of the import text
        TextRange range = TextRange.from(element.getTextRange().getStartOffset(), element.getTextLength());
        createAnnotation(holder, range, importColor);
    }

    private static void getClassBlockColor(@NotNull PsiClass clazz, @NotNull AnnotationHolder holder)
    {
        // Get the children elements which are all the elements included in a class definition
        PsiElement[] elements = clazz.getChildren();

        // Iterate over each child element
        for (PsiElement e : elements)
        {
            if (e instanceof PsiModifierList || (e instanceof PsiKeyword && e.getText().equals("class")))
            {
                // If we have a modifier (i.e. public) or the class keyword get the range of text + 1 for whitespace
                TextRange range = TextRange.from(e.getTextRange().getStartOffset(), e.getTextLength() + 1);
                createAnnotation(holder, range, classColor);
            }
        }

        // Get the class name and text range
        PsiIdentifier name = clazz.getNameIdentifier();

        if (name != null)
        {
            // If the name exists, color it
            TextRange nameRange = TextRange.from(name.getTextRange().getStartOffset(), name.getTextLength());
            createAnnotation(holder, nameRange, classColor);
        }

        // Get the left brace
        PsiElement lbrace = clazz.getLBrace();

        if (lbrace != null)
        {
            TextRange lbRange = TextRange.from(lbrace.getTextRange().getStartOffset(), lbrace.getTextLength());
            createAnnotation(holder, lbRange, classColor);
        }

        // Get the right brace
        PsiElement rbrace = clazz.getRBrace();

        if (rbrace != null)
        {
            TextRange rbRange = TextRange.from(rbrace.getTextRange().getStartOffset(), rbrace.getTextLength());
            createAnnotation(holder, rbRange, classColor);
        }

        // Get all the fields declared in the class
        PsiField[] fields = clazz.getAllFields();

        for (PsiField field : fields)
        {
            // Color each field, if any
            TextRange fieldRange = TextRange.from(field.getTextRange().getStartOffset(), field.getTextLength());
            createAnnotation(holder, fieldRange, classColor);
        }
    }

    private static void getMethodBlockColor(@NotNull PsiMethod method, @NotNull AnnotationHolder holder)
    {
        // Get the modifier list
        PsiModifierList modifierList = method.getModifierList();

        // Get the text range of the modifier list
        TextRange modRange = TextRange.from(modifierList.getTextRange().getStartOffset(), modifierList.getTextLength() + 1);
        createAnnotation(holder, modRange, methodColor);

        // Get the return type
        PsiTypeElement ret = method.getReturnTypeElement();

        if (ret != null)
        {
            // Get the text range of the return
            TextRange retRange = TextRange.from(ret.getTextRange().getStartOffset(), ret.getTextLength() + 1);
            createAnnotation(holder, retRange, methodColor);
        }

        // Get the name of the method
        PsiIdentifier id = method.getNameIdentifier();

        if (id != null)
        {
            // Get the text range of the method name
            TextRange idRange = TextRange.from(id.getTextRange().getStartOffset(), id.getTextLength());
            createAnnotation(holder, idRange, methodColor);
        }

        // Get the parameters
        PsiParameterList parameterList = method.getParameterList();

        // Get the text range of the parameter list
        TextRange paramRange = TextRange.from(parameterList.getTextRange().getStartOffset(), parameterList.getTextLength());
        createAnnotation(holder, paramRange, methodColor);

        // Get the body code block
        PsiCodeBlock body = method.getBody();

        if (body != null)
        {
            // Color the braces
            getBraceBlockColor(body, holder, methodColor);
        }

    }

    private static void getConditionBlockColor(@NotNull PsiConditionalLoopStatement element, @NotNull AnnotationHolder holder)
    {
        // Get the children element
        PsiElement[] elements = element.getChildren();

        // Iterate over the children elements
        for (PsiElement e : elements)
        {
            if (e instanceof PsiKeyword)
            {
                // Get the range + 1 for whitespace of the keyword
                TextRange range = TextRange.from(e.getTextRange().getStartOffset(), e.getTextLength() + 1);
                createAnnotation(holder, range, conditionColor);
            }
            else if (e instanceof PsiBlockStatement)
            {
                // Get the code block for the right and left brace tokens
                PsiCodeBlock codeBlock = ((PsiBlockStatement) e).getCodeBlock();
                getBraceBlockColor(codeBlock, holder, conditionColor);
            }
        }

        // Get the condition
        PsiExpression expression = element.getCondition();

        if (expression != null)
        {
            TextRange exprRange = TextRange.from(expression.getTextRange().getStartOffset(), expression.getTextLength());
            createAnnotation(holder, exprRange, conditionColor);
        }
    }

    private static void getWhileBlockColor(@NotNull PsiWhileStatement whileStatement, @NotNull AnnotationHolder holder)
    {
        // Get the left parentheses
        PsiElement lParenth = whileStatement.getLParenth();

        if (lParenth != null)
        {
            TextRange lPRange = TextRange.from(lParenth.getTextRange().getStartOffset(), lParenth.getTextLength());
            createAnnotation(holder, lPRange, conditionColor);
        }

        // Get the right parentheses
        PsiElement rParenth = whileStatement.getRParenth();

        if (rParenth != null)
        {
            TextRange rPRange = TextRange.from(rParenth.getTextRange().getStartOffset(), rParenth.getTextLength());
            createAnnotation(holder, rPRange, conditionColor);
        }
    }

    private static void getDoWhileBlockColor(@NotNull PsiDoWhileStatement doWhileStatement, @NotNull AnnotationHolder holder)
    {
        // Get the left parentheses
        PsiElement lParenth = doWhileStatement.getLParenth();

        if (lParenth != null)
        {
            TextRange lPRange = TextRange.from(lParenth.getTextRange().getStartOffset(), lParenth.getTextLength());
            createAnnotation(holder, lPRange, conditionColor);
        }

        // Get the right parentheses
        PsiElement rParenth = doWhileStatement.getRParenth();

        if (rParenth != null)
        {
            TextRange rPRange = TextRange.from(rParenth.getTextRange().getStartOffset(), rParenth.getTextLength());
            createAnnotation(holder, rPRange, conditionColor);
        }
    }

    private static void getForBlockColor(@NotNull PsiForStatement forStatement, @NotNull AnnotationHolder holder)
    {
        // Get the initialization statement
        PsiStatement init = forStatement.getInitialization();

        if (init != null)
        {
            // If there is an initialization, color it
            TextRange initRange = TextRange.from(init.getTextRange().getStartOffset(), init.getTextLength());
            createAnnotation(holder, initRange, conditionColor);
        }

        // Get the update statement
        PsiStatement update = forStatement.getUpdate();

        if (update != null)
        {
            TextRange updateRange = TextRange.from(update.getTextRange().getStartOffset(), update.getTextLength());
            createAnnotation(holder, updateRange, conditionColor);
        }

        // Get the left parentheses
        PsiElement lParenth = forStatement.getLParenth();

        if (lParenth != null)
        {
            TextRange lPRange = TextRange.from(lParenth.getTextRange().getStartOffset(), lParenth.getTextLength());
            createAnnotation(holder, lPRange, conditionColor);
        }

        // Get the right parentheses
        PsiElement rParenth = forStatement.getRParenth();

        if (rParenth != null)
        {
            TextRange rPRange = TextRange.from(rParenth.getTextRange().getStartOffset(), rParenth.getTextLength());
            createAnnotation(holder, rPRange, conditionColor);
        }

    }

    private static void getIfBlockColor(@NotNull PsiIfStatement ifStatement, @NotNull AnnotationHolder holder)
    {
        Project project = ifStatement.getProject();
        PsiFile file = ifStatement.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);

        // Get the children element
        PsiElement[] elements = ifStatement.getChildren();

        // Iterate over the children elements
        for (PsiElement e : elements)
        {
            if (e instanceof PsiKeyword)
            {
                // Get the range + 1 for whitespace of the keyword
                TextRange range = TextRange.from(e.getTextRange().getStartOffset(), e.getTextLength() + 1);
                createAnnotation(holder, range, conditionColor);
            }
            else if (e instanceof PsiBlockStatement)
            {
                // Get the code block for the right and left brace tokens
                PsiCodeBlock codeBlock = ((PsiBlockStatement) e).getCodeBlock();
                getBraceBlockColor(codeBlock, holder, conditionColor);

                // Get the indent color
                getIndentBlockColor(codeBlock, holder, document, conditionColor);
            }
        }

        // Get the condition
        PsiExpression expression = ifStatement.getCondition();

        if (expression != null)
        {
            TextRange exprRange = TextRange.from(expression.getTextRange().getStartOffset(), expression.getTextLength());
            createAnnotation(holder, exprRange, conditionColor);
        }

        // Get the left parentheses
        PsiElement lParenth = ifStatement.getLParenth();

        if (lParenth != null)
        {
            TextRange lPRange = TextRange.from(lParenth.getTextRange().getStartOffset(), lParenth.getTextLength());
            createAnnotation(holder, lPRange, conditionColor);
        }

        // Get the right parentheses
        PsiElement rParenth = ifStatement.getRParenth();

        if (rParenth != null)
        {
            TextRange rPRange = TextRange.from(rParenth.getTextRange().getStartOffset(), rParenth.getTextLength());
            createAnnotation(holder, rPRange, conditionColor);
        }
    }

    private static void getTryBlockColor(@NotNull PsiTryStatement tryStatement, @NotNull AnnotationHolder holder)
    {
        // Get the children elements
        PsiElement[] elements = tryStatement.getChildren();

        for (PsiElement element : elements)
        {
            if (element instanceof PsiKeyword)
            {
                // Color the try keyword
                TextRange range = TextRange.from(element.getTextRange().getStartOffset(), element.getTextLength() + 1);
                createAnnotation(holder, range, conditionColor);
            }
        }

        PsiCodeBlock tryBlock = tryStatement.getTryBlock();

        if (tryBlock != null)
        {
            // Color the braces on the try block portion
            getBraceBlockColor(tryBlock, holder, conditionColor);
        }

        // Get all of the catch sections
        PsiCatchSection[] catchSections = tryStatement.getCatchSections();

        for (PsiCatchSection catchSection : catchSections)
        {
            // Get the catch section's children
            PsiElement[] catchElements = catchSection.getChildren();

            for (PsiElement catchElement : catchElements)
            {
                if (catchElement instanceof PsiKeyword)
                {
                    // Color the catch keyword
                    TextRange range = TextRange.from(catchElement.getTextRange().getStartOffset(), catchElement.getTextLength() + 1);
                    createAnnotation(holder, range, conditionColor);
                }
            }

            // Get the parameter of the current catch section
            PsiParameter parameter = catchSection.getParameter();

            if (parameter != null)
            {
                TextRange range = TextRange.from(parameter.getTextRange().getStartOffset(), parameter.getTextLength() + 1);
                createAnnotation(holder, range, conditionColor);
            }

            // Get the left parentheses
            PsiElement lParenth = catchSection.getLParenth();

            if (lParenth != null)
            {
                TextRange lPRange = TextRange.from(lParenth.getTextRange().getStartOffset(), lParenth.getTextLength());
                createAnnotation(holder, lPRange, conditionColor);
            }

            // Get the right parentheses
            PsiElement rParenth = catchSection.getRParenth();

            if (rParenth != null)
            {
                TextRange rPRange = TextRange.from(rParenth.getTextRange().getStartOffset(), rParenth.getTextLength());
                createAnnotation(holder, rPRange, conditionColor);
            }

            // Get the catch code block
            PsiCodeBlock codeBlock = catchSection.getCatchBlock();

            if (codeBlock != null)
            {
                // Color the catch code block braces
                getBraceBlockColor(codeBlock, holder, conditionColor);
            }
        }
    }

    private static void getBraceBlockColor(@NotNull PsiCodeBlock codeBlock, @NotNull AnnotationHolder holder, Color color)
    {
        // Get the left brace
        PsiJavaToken lBrace = codeBlock.getLBrace();

        if (lBrace != null)
        {
            // If the left brace exists color it with the designated color
            TextRange lBRange = TextRange.from(lBrace.getTextRange().getStartOffset(), lBrace.getTextLength());
            createAnnotation(holder, lBRange, color);
        }

        // Get the right brace
        PsiJavaToken rBrace = codeBlock.getRBrace();

        if (rBrace != null)
        {
            // If the right brace exists color it with the designated color
            TextRange rBRange = TextRange.from(rBrace.getTextRange().getStartOffset(), rBrace.getTextLength());
            createAnnotation(holder, rBRange, color);
        }
    }

    private static void getIndentBlockColor(@NotNull PsiCodeBlock codeBlock, @NotNull AnnotationHolder holder,
                                            Document document, Color color)
    {
        if (document == null)
        {
            // Return if the document does not exist
            return;
        }

        // Get the statements in the code block
        PsiStatement[] statements = codeBlock.getStatements();

        // Get the start and end offset of the code block (not including the braces)
        int startOffset = statements[0].getTextRange().getStartOffset();
        int endOffset = statements[statements.length - 1].getTextRange().getEndOffset();

        // Get the line numbers of the starting and ending lines of the code block
        int startLine = document.getLineNumber(startOffset);
        int endLine = document.getLineNumber(endOffset);

        // Iterate over each line in the code block
        for (int i = startLine; i <= endLine; i++)
        {
            // Get the start and end offset of the current line
            int start = document.getLineStartOffset(i);
            int end = document.getLineEndOffset(i);

            // Get the text of the line based on the offsets
            String text = document.getText(TextRange.from(start, end - start));

            int j = 0;
            for (; j < text.length(); j++)
            {
                // Count the number of space or tab characters for the indent
                if (((int) text.charAt(j) != 32) && ((int) text.charAt(j) != 9))
                {
                    // Count until a character that isn't a space or tab is encountered
                    break;
                }
            }

            // Create an annotation for the indent of the line
            TextRange range = TextRange.from(start, j);
            createAnnotation(holder, range, color);
        }
    }

    private static void getStatementBlockColor(@NotNull PsiElement element, @NotNull AnnotationHolder holder)
    {
        // Get the range of the text
        TextRange range = TextRange.from(element.getTextRange().getStartOffset(), element.getTextLength());
        createAnnotation(holder, range, statementColor);
    }

    // TODO: remove or use if necessary
//    public static void colorMethodBlock(@NotNull PsiElement element, @NotNull AnnotationHolder holder)
//    {
//        PsiElement[] children = element.getChildren();
//        for (PsiElement child : children)
//        {
//            System.out.println(child);
//        }
//        PsiElement lastChild = element.getLastChild();
//        int end = lastChild.getStartOffsetInParent();
//        TextRange range = TextRange.from(element.getTextRange().getStartOffset(), end);
//        createAnnotation(holder, range, methodColor);
//    }
}
