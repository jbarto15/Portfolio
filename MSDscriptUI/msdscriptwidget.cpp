#include "msdscriptwidget.h"
#include "parse.hpp"
#include "Expr.h"
#include "Val.h"


//constructor
MSDscriptWidget::MSDscriptWidget(QWidget *parent) : QWidget{parent}
{



    //create widgets
    main_layout = new QGridLayout;
    expression_box = new QGridLayout;
    expression_label = new QLabel("Expression:");
    expression_text = new QTextEdit;
    radio_buttons_box = new QGroupBox;
    radio_buttons_layout = new QVBoxLayout;
    interp_button = new QRadioButton("Interp");
    print_button = new QRadioButton("Pretty Print");
    submit_button = new QPushButton("Submit");
    result_box = new QGridLayout;
    result_label = new QLabel("Result:");
    result_text = new QTextEdit;
    reset_button = new QPushButton("Reset");
    horizontalSpacer1 = new QSpacerItem(20, 20, QSizePolicy::Fixed, QSizePolicy::Minimum);
    horizontalSpacer2 = new QSpacerItem(45, 20, QSizePolicy::Fixed, QSizePolicy::Minimum);


    //set width of text box and buttons
    int text_box_width = 200;
    int button_width = 100;

    submit_button->setMinimumWidth(button_width);
    submit_button->setMaximumWidth(button_width);

    reset_button->setMinimumWidth(button_width);
    reset_button->setMaximumWidth(button_width);


    //set the text box sizes
    expression_text->setMinimumWidth(text_box_width);
    result_text->setMinimumWidth(text_box_width);


    //add the radio buttons to the radio buttons layout
    radio_buttons_layout->addWidget(interp_button);
    radio_buttons_layout->addWidget(print_button);


    //add the radio buttons layout to the radio buttons group box
    radio_buttons_box->setLayout(radio_buttons_layout);


    //add all the widgets to the main layout
    main_layout->addWidget(expression_label, 0, 0);
    main_layout->addWidget(expression_text, 0, 2);
    main_layout->addWidget(radio_buttons_box, 1, 2);
    main_layout->addWidget(submit_button, 2, 2);
    main_layout->addWidget(result_label, 3, 0);
    main_layout->addWidget(result_text, 3, 2);
    main_layout->addWidget(reset_button, 4, 2);


    //set the layout
    this->setLayout(main_layout);


    //connect calculateResult to click
    connect(submit_button, &QPushButton::clicked, this, &MSDscriptWidget::calculateResult);


    //connect resetWindow to click
    connect(reset_button, &QPushButton::clicked, this, &MSDscriptWidget::resetWindow);


}



void MSDscriptWidget::calculateResult() {

    //get the text from the expression text
    QString expression = expression_text->toPlainText();

    //convert the QString into a normal string
    std::string str_to_be_parsed = expression.toUtf8().constData();

    //get the info from the radio buttons
    QString type_of_operation;

    if ( interp_button->isChecked() ) {

        type_of_operation = "Interp";

    } else if ( print_button->isChecked() ) {

        type_of_operation = "Print";

    } else {

        type_of_operation = "none";

    }



    if ( expression != nullptr && type_of_operation != "none" ) {


        //parse the expression string
        PTR(Expr) obj = parse_str( str_to_be_parsed );

        QString result_to_display;

        if ( type_of_operation == "Print" ) {

            //convert the object into a string
            result_to_display = QString::fromStdString(obj->to_string_pretty());

        } else {

            result_to_display = QString::fromStdString(obj->interp()->to_string());

        }



        //display result in the result text area
        result_text->setPlainText(result_to_display);

    }



}

void MSDscriptWidget::resetWindow() {


    expression_text->clear();
    result_text->clear();

    //to clear the radio buttons
    QList <QRadioButton *> listRadioButtons = radio_buttons_box->findChildren<QRadioButton *>();

    for ( auto radio : listRadioButtons ) {

        radio->setAutoExclusive(0);
        radio->setChecked(false);
        radio->setAutoExclusive(1);
    }



}








