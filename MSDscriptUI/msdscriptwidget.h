#ifndef MSDSCRIPTWIDGET_H
#define MSDSCRIPTWIDGET_H

#include <QWidget>
#include <QtWidgets>

class MSDscriptWidget : public QWidget
{
    Q_OBJECT


public:

    explicit MSDscriptWidget(QWidget *parent = nullptr);


private:

    QGridLayout *main_layout;
    QGridLayout *expression_box;
    QLabel *expression_label;
    QTextEdit *expression_text;
    QGroupBox *radio_buttons_box;
    QVBoxLayout *radio_buttons_layout;
    QRadioButton *interp_button;
    QRadioButton *print_button;
    QPushButton *submit_button;
    QGridLayout *result_box;
    QLabel *result_label;
    QTextEdit *result_text;
    QPushButton *reset_button;
    QSpacerItem *horizontalSpacer1;
    QSpacerItem *horizontalSpacer2;


private slots:

    void calculateResult();

    void resetWindow();



signals:


};

#endif // MSDSCRIPTWIDGET_H
