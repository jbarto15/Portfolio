#include <QApplication>
#include "msdscriptwidget.h"

int main(int argc, char *argv[])
{

    QApplication app ( argc, argv );

    MSDscriptWidget *myWidget = new MSDscriptWidget();

    myWidget->show();


    return app.exec();
}
