#!/opt/local/Library/Frameworks/Python.framework/Versions/2.7/bin/python
import math

from numpy import savetxt, loadtxt, genfromtxt
from sklearn.ensemble import forest
from sklearn.externals import joblib
from sklearn import svm, metrics, tree, ensemble, naive_bayes, lda, qda, cluster, neighbors
from os.path import exists
import sys

import logging


logging.getLogger().setLevel(logging.DEBUG)

general_path = '/Users/alashchenko/Development/Workspace/LodeRunnerBot/PyBot/'
classifier_dump = general_path + 'classifier.dump'
if not exists(classifier_dump):
    classifier = cluster.KMeans(n_clusters=6)
    # cluster.neural_network.

    # classifier = neighbors.KNeighborsClassifier(n_neighbors=6, weights='uniform')

    # classifier = forest.RandomForestClassifier(n_estimators=512, n_jobs=8)

    # classifier = svm.SVC(kernel="poly", degree=2)
    # classifier = svm.SVC(kernel="poly", C=1e3)
    # classifier = svm.SVR(kernel="rbf", C=1e3, gamma=0.1)

    # classifier = svm.SVR(kernel="rbf")

    # classifier = KNeighborsClassifier(3)
    # classifier = svm.SVC(kernel="linear", C=0.025)
    # classifier = svm.SVC(kernel="linear", C=1e3)

    # classifier = svm.SVC(gamma=2, C=1)

    # classifier = tree.DecisionTreeClassifier(max_depth=1)
    # classifier = RandomForestClassifier(max_depth=5, n_estimators=10, max_features=1)
    # classifier = RandomForestClassifier(max_depth=6, n_estimators=512, max_features=None)
    # classifier = RandomForestClassifier(n_estimators=512)
    # classifier = ensemble.AdaBoostClassifier()
    # classifier = naive_bayes.GaussianNB()
    # classifier = lda.LDA()
    # classifier = qda.QDA()
    logging.debug("Make new {} ...".format(classifier))

    logging.debug("Loading train set ...")

    train_csv = general_path + 'train.csv'
    train_dump = general_path + 'train.dump'
    if not exists(train_dump):
        logging.debug("Dump file {} do not exist. Try create it from {}.".format(train_dump, train_csv))
        # data = loadtxt(open(train_csv, 'r'), delimiter=',', skiprows=0)
        # data = genfromtxt(train_csv, delimiter=",", filling_values=9788)
        data = genfromtxt(train_csv, delimiter=",", invalid_raise=False)
        # data_clean = [line for line in data if ]
        logging.debug("Train data len is: {}".format(len(data)))
        joblib.dump(data, train_dump)

    logging.debug("Dump file {} exist. Try load it.".format(train_dump))
    train_set = joblib.load(train_dump)

    logging.debug("Split train set to target/train and start to train our classifier ...")
    target = [x[0] for x in train_set]  # action
    train = [x[1:] for x in train_set]  # board

    classifier.fit(train, target)
    logging.debug("Learning complete so create dump of classifier ...")
    joblib.dump(classifier, classifier_dump)


classifier = joblib.load(classifier_dump)
logging.debug("Predict ...")

board = sys.argv[1]
answer = classifier.predict(board.split(','))
logging.debug(" ... and answer is {}".format(answer[0]))

# exit(int((answer[0])))
# exit(int(round(answer[0])))
exit(int((answer[0])))
