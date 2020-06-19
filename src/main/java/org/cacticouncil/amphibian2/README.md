#### General Information

All provided IntelliJ classes and methods can be found through https://github.com/JetBrains/intellij-community. 
General plugin documentation can be found at https://www.jetbrains.org/intellij/sdk/docs/intro/welcome.html.

#### Amphibian2Annotator.java

This class overrides the default annotator, overriding the annotate method. Annotate takes in a
PsiElement and an AnnotationHolder to highlight each PsiElement in the currently opened file.

#### Amphibian2ColorGenerator.java

This class determines the specific syntax elements that need to be highlighted with what color.
An annotation is made to create the highlighting.

#### Amphibian2EditorColorsListener.java

This class overrides the EditorColorsListener to update the scheme on a color scheme change.