package com.esri.core.geometry;

public abstract class OperatorImportFromMGRS extends Operator {
    @Override
    public Type getType() {
        return Type.ImportFromMGRS;
    }

    public abstract GeometryCursor execute(SimpleStringCursor stringCursor,
                                           ProgressTracker progress_tracker);

    public abstract Geometry execute(Geometry.Type type,
                                     String string,
                                     ProgressTracker progress_tracker);

    public static OperatorImportFromMGRS local() {
        return (OperatorImportFromMGRS) OperatorFactoryLocal.getInstance()
                .getOperator(Type.ImportFromMGRS);
    }
}
