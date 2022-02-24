package king.notepad.service;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Calendar;

import javax.swing.JOptionPane;

import king.notepad.view.FontDialog;
import king.notepad.view.GotoDialog;
import king.notepad.view.NotepadFrame;
import king.notepad.view.findreplacedialog.MyDialog;

// Бизнес-код, связанный с текстовыми операциями
public class TextService {
    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    // Вставить дату и время
    public static void addTime(NotepadFrame frame){
        Calendar c = Calendar.getInstance();
        String time = "" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE)
                + " " + c.get(Calendar.YEAR) + "/" + c.get(Calendar.MONTH)
                + "/" + c.get(Calendar.DATE);
        frame.replaceSelection(time);
    }

    //выбрать все
    public static void allSelect(NotepadFrame frame){
        frame.selectAll();
    }

    // Копировать
    public static void copy(NotepadFrame frame){
        // Копировать в системный буфер обмена
        String selectText = frame.getSelectedText();
        // Поместить в буфер обмена после упаковки
        StringSelection select = new StringSelection(selectText);
        clipboard.setContents(select, null);
    }

    //Резать
    public static void cut(NotepadFrame frame){
        // Копируем и затем удаляем выбранный контент
        copy(frame);
        delete(frame);
    }

    // Удалить выбранный контент
    public static void delete(NotepadFrame frame){
        frame.replaceSelection("");
    }

    //Вставить
    public static void paste(NotepadFrame frame){
        // Если выбрано содержимое, замените его, если нет, вставьте позицию курсора
        frame.replaceSelection(getClipboardText());
    }

    // Получить текст в буфер обмена
    // Если текста нет, возвращаем пустую строку
    private static String getClipboardText(){
        // Если в буфере обмена есть текст, вернуть текст
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
            try{
                return (String)clipboard.getData(DataFlavor.stringFlavor);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return "";
    }


    /**
     * Найти
     * @param dialog Найти или заменить и заменить диалоговое окно
     */
    public static void find(MyDialog dialog){
        String findText = dialog.getFindText (); // Строка для поиска
        String text = dialog.getWholeText (); // Весь текст
        int start = 0; // Начальный индекс текстовой области для поиска
        int end = text.length (); // индекс в конце текстовой области для поиска
        // Если он не чувствителен к регистру, то все строчные
        if (!dialog.isMatchCase()){
            text = text.toLowerCase();
            findText = findText.toLowerCase();
        }
        if (dialog.isDownward ()) {// Если смотреть вниз, для поиска используется только текст после курсора
            start = dialog.getSelectionEnd();
            start = text.indexOf(findText, start);
            if (start == -1) {// Подсказка появится, если текст не найден
                JOptionPane.showMessageDialog (dialog.getNotepadFrame (), "Не удается найти \" "+ findText +" \ ""
                        , NotepadFrame.PROGRAM_NAME.substring(1), JOptionPane.INFORMATION_MESSAGE);
            } else {// Выбрать текст, если найден
                dialog.select(start, start + findText.length());
            }
        } else {// Если вы посмотрите вверх, возьмите текст перед курсором
            end = dialog.getSelectionStart();
            end = text.lastIndexOf(findText, end);
            if(end == -1){
                JOptionPane.showMessageDialog (dialog.getNotepadFrame (), "Не удается найти \" "+ findText +" \ ""
                        , NotepadFrame.PROGRAM_NAME.substring(1), JOptionPane.INFORMATION_MESSAGE);
            }else{
                dialog.select(end, end + findText.length());
            }
        }
    }

    // Заменить
    public static void replace(MyDialog dialog){
        String selectedText = dialog.getSelectedText (); // Текущий выделенный текст
        String findText = dialog.getFindText (); // текст для поиска
        // Если он не чувствителен к регистру, то все строчные
        if (!dialog.isMatchCase()){
            findText = findText.toLowerCase();
            if (selectedText != null) selectedText = selectedText.toLowerCase();
        }
        // Если выделенный текст является строкой, которую нужно найти, замените
        if(findText.equals(selectedText)){
            dialog.replaceSelection (dialog.getReplaceText ()); // Заменить строкой перед преобразованием
        }
        // Независимо от того, была ли замена на предыдущем шаге, вы должны найти ее один раз
        find (dialog); // Заменить можно только вниз.
    }

    //Заменить все
    public static void replaceAll(MyDialog dialog){
        String text = dialog.getWholeText (); // Весь текст
        String findText = dialog.getFindText (); // текст для поиска
        // Будь чувствителен к регистру
        if (!dialog.isMatchCase()){
            text = text.toLowerCase();
            findText = findText.toLowerCase();
        }
        // Заменить ниже
        text = text.replace(findText, dialog.getReplaceText());
        dialog.setText(text);
    }
    // Получить количество строк, где находится курсор
    public static int getRow(NotepadFrame frame){
        int count = 0; // Перед записью есть несколько возвратов каретки
        int index = frame.getSelectionStart (); // Положение курсора,
        String text = frame.getWholeText (); // все текстовое поле, используемое для поиска
        // перед позицией курсора есть несколько разрывов строк
        while((index = text.lastIndexOf("\n", index)) != -1){
            count++;
            index--; // Повторно возвращаться в эту позицию без уменьшения на 1
        }
        count ++; // впереди есть символ возврата каретки, указывающий на наличие двух строк
        // Если есть возврат каретки сразу после этого, он уменьшается на 1, что является выводом из реальной ситуации
        if (text.length() > frame.getSelectionStart() && text.charAt(frame.getSelectionStart()) == '\n') count--;
        return count;
    }

    // Получить количество столбцов, в которых находится курсор
    public static int getColumn(NotepadFrame frame){
        int index = frame.getSelectionStart (); // Положение курсора
        String text = frame.getWholeText (); // Все содержимое в текстовом поле
        text = text.substring(0, index);
//     int start = text.lastIndexOf("\n", index) == -1 ? 0 : text.lastIndexOf("\n", index);
        int start = text.lastIndexOf("\n", index);
        if (start == -1) start = 0;
        int count = text.substring(start, index).length();
        if (start == 0) count ++; // Приведенный выше алгоритм, если он находится в первой строке, в первом столбце равен 0 и должен быть увеличен на 1.
        return count;
    }

    // Вставляем временное хранилище в стек, сохраняем содержимое текстового поля во временное хранилище и устанавливаем меню «Отменить», чтобы оно было видимым
    public static void pushTextArea(NotepadFrame frame){
        String text = frame.getWholeText();
        if (text.length() > 0 || text == ""){
            frame.pushToStack(frame.getTmpText());
            frame.setTmpText(text);
            frame.setRepealMenuItemEnabled(true);
        }
    }

    // выталкиваем содержимое текстового поля из стека, то есть операция «отменить»
    public static void popTextArea(NotepadFrame frame){
        // Содержимое в стеке не равно 0, затем всплывающее окно
        if(frame.getStackSize() > 1){
            String text = frame.popFromStack();
            frame.setText (text); // В это время слушатель запускается, чтобы поместить этот текст в стек
            frame.popFromStack();
        }
        else {// Нет содержимого в стеке, установите меню отмены невидимым и установите для текста нулевое значение
            frame.setRepealMenuItemEnabled(false);
            frame.setText("");
        }
    }
    // Меню «Обтекание», установка переноса слов и строки состояния

    public static void setAutoWrap(NotepadFrame frame){

        frame.setLineWrap (! frame.getLineWrap ()); // Изменить состояние переноса слов

// Если поведение автоматического изменения истинно, строка состояния не отображается и кнопка меню строки состояния отключена

        if (frame.getLineWrap()){

            frame.setStatePanelVisible(false);

            frame.setStateMenuItemEnabled(false);

        }else{

            frame.setStatePanelVisible((frame.getStatePanelMenuItem()));

            frame.setStateMenuItemEnabled(true);

        }

    }



    // Меню "Go", всплывает диалоговое окно

    public static void gotoLine(GotoDialog dialog){

        int lineNum = dialog.getLineNum (); // Количество пропущенных строк

        NotepadFrame frame = dialog.getNotepadFrame();

        String text = dialog.getWholeText (); // Весь текст

        int index = 0; // номер строки

        // Если строк больше, чем есть, появится подсказка

        if (lineNum > dialog.getLineCount()){

            JOptionPane.showMessageDialog (frame, «Количество строк превышает общее количество строк», frame.PROGRAM_NAME.substring (1) +

                    "-Jump line", JOptionPane.ERROR_MESSAGE);

            dialog.setLineNum(getRow(frame));

        }else{

            while (lineNum> 1) {// пропустить возврат каретки lineNum-1

                index = text.indexOf("\n", index);

                index ++; // Переместить один, чтобы продолжить поиск

                lineNum--; // Уменьшить 1, найдя возврат каретки;

            }

            dialog.setCaretPosition(index);

            dialog.dispose();

        }

    }



    // Получить шрифт, выбранный в диалоге шрифта

    private static Font getSelectedFont(FontDialog dialog){

        return new Font(dialog.getFontListValue()

                , dialog.getFontStyleListValue(), dialog.getFontSizeListValue());

    }



    // Установить образец шрифта Label

    private static void setExampleFont(FontDialog dialog){

        // Изменить шрифт примера Label

        dialog.setExampleFont(getSelectedFont(dialog));

    }



    // Установить шрифт текстового поля и закрыть диалог

    public static void setTextAreaFont(FontDialog dialog){

        dialog.setTextAreaFont(getSelectedFont(dialog));

        dialog.dispose();

    }



    // Устанавливаем текст примера Label

    public static void setTextExample(FontDialog dialog){

        dialog.setExampleText(dialog.getScenarioListValue());

    }



    // Действие при выборе списка шрифтов

    // Отображение выбранных элементов списка глифов в верхнем текстовом поле и изменение шрифта в примере

    public static void setFontListSelected(FontDialog dialog){

        // Отображение выбранных элементов шрифта JList в верхнем текстовом поле

        dialog.setFontTextField(dialog.getFontListValue());

        setExampleFont(dialog);

    }



    // Действие при выборе списка глифов

    // Отображение выбранных элементов списка глифов в верхнем текстовом поле и изменение шрифта в примере

    public static void setFontStyleListSelected(FontDialog dialog){

        // Отображение выбранных элементов списка глифов в верхнем текстовом поле

        dialog.setFontStyleTextField(FontDialog.fontStyle[dialog.getFontStyleListValue()]);

        setExampleFont(dialog);

    }



    // Действие при выборе списка размеров шрифта

    // Отображение выбранных элементов в списке размеров шрифта в верхнем текстовом поле и изменение шрифта в примере

    public static void setFontSizeListSelected(FontDialog dialog){

        // Отображение выбранного элемента списка размеров шрифта в верхнем текстовом поле

        dialog.setFontSizeTextField(dialog.getFontSizeListValue());

        setExampleFont(dialog);

    }

}
