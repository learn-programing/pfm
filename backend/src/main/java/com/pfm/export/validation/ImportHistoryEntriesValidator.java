package com.pfm.export.validation;

import com.pfm.history.HistoryEntry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ImportHistoryEntriesValidator extends HelperValidator {

  private static final String DATA_NAME = "history entries";
  private static final String ID = " ID;";
  private static final String DATE = " date;";
  private static final String OBJECT = " object;";
  private static final String TYPE = " type;";
  private static final String ENTRIES = " entries;";
  private static final String CHILD_ID = " id;";
  private static final String CHILD_NAME = " name;";
  private static final String CHILD_NEW_VALUE = " new value;";
  private static final String CHILD_OLD_VALUE = " old value;";

  List<String> validate(List<HistoryEntry> inputData) {

    List<String> validationResult = new ArrayList<>();

    for (int i = 0; i < inputData.size(); i++) {

      StringBuilder incorrectFields = new StringBuilder();

      if (checkDataMissing(inputData.get(i).getId())) {
        incorrectFields.append(ID);
      }
      if (checkDataMissing(inputData.get(i).getDate())) {
        incorrectFields.append(DATE);
      }
      if (checkDataMissing(inputData.get(i).getObject())) {
        incorrectFields.append(OBJECT);
      }
      if (checkDataMissing(inputData.get(i).getType())) {
        incorrectFields.append(TYPE);
      }
      if (inputData.get(i).getEntries() == null) {
        incorrectFields.append(ENTRIES);
      } else {
        for (int j = 0; j < inputData.get(i).getEntries().size(); j++) {

          StringBuilder incorrectChildFields = new StringBuilder();

          if (checkDataMissing(inputData.get(i).getEntries().get(j).getId())) {
            incorrectChildFields.append(CHILD_ID);
          }
          if (checkDataMissing(inputData.get(i).getEntries().get(j).getName())) {
            incorrectChildFields.append(CHILD_NAME);
          }
          if (checkDataMissing(inputData.get(i).getEntries().get(j).getNewValue())) {
            incorrectChildFields.append(CHILD_NEW_VALUE);
          }
          if (checkDataMissing(inputData.get(i).getEntries().get(j).getOldValue())) {
            incorrectChildFields.append(CHILD_OLD_VALUE);
          }

          if (incorrectChildFields.length() > 0) {
            incorrectFields.append(getChildMessage(incorrectChildFields.toString(), j));
          }
        }
      }

      if (incorrectFields.length() > 0) {
        validationResult.add(createResultMessage(DATA_NAME, i, incorrectFields.toString()));
      }
    }

    return validationResult;
  }

  private String getChildMessage(String incorrectFields, int numberInRow) {
    return " missing in entry number:" + numberInRow + incorrectFields;
  }
}