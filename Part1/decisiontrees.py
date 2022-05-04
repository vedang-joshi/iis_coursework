import pandas as pd
from sklearn.tree import DecisionTreeClassifier # Import Decision Tree Classifier
from sklearn.tree import export_graphviz
from six import StringIO
import pydotplus
from sklearn.metrics import classification_report,confusion_matrix,\
    brier_score_loss, roc_curve, roc_auc_score,log_loss
import matplotlib.pyplot as plt
import numpy as np
from sklearn.tree import _tree

dataset = pd.read_csv('Data/iis_dataset.csv')
# Label dataset into data and target:
feature_cols = ['Willingness_to_follow_order', 'Existence_of_physical_disabilities',
                'Willingness_to_help_peers', 'Running_towards_fire']

X = dataset[feature_cols] # Features
y = dataset.Does_student_pose_a_risk_during_evacuation # Target variable

classnames=['Low supervision','High supervision']

clf = DecisionTreeClassifier(class_weight='balanced')

# Train Decision Tree Classifer
dt = clf.fit(X,y)

dot_data = StringIO()
export_graphviz(dt, out_file=dot_data,
                filled=True, rounded=True,
                special_characters=True,feature_names = feature_cols,class_names=['Low supervision','High supervision'])
graph = pydotplus.graph_from_dot_data(dot_data.getvalue())
graph.write_pdf('decision_tree.pdf')


# Extract rules:

def get_rules(tree, feature_names, class_names):
    tree_ = tree.tree_
    feature_name = [
        feature_names[i] if i != _tree.TREE_UNDEFINED else "undefined!"
        for i in tree_.feature
    ]

    paths = []
    path = []

    def recurse(node, path, paths):

        if tree_.feature[node] != _tree.TREE_UNDEFINED:
            name = feature_name[node]
            threshold = tree_.threshold[node]
            p1, p2 = list(path), list(path)
            p1 += [f"({name} <= {np.round(threshold, 3)})"]
            recurse(tree_.children_left[node], p1, paths)
            p2 += [f"({name} > {np.round(threshold, 3)})"]
            recurse(tree_.children_right[node], p2, paths)
        else:
            path += [(tree_.value[node], tree_.n_node_samples[node])]
            paths += [path]

    recurse(0, path, paths)

    # sort by samples count
    samples_count = [p[-1][1] for p in paths]
    ii = list(np.argsort(samples_count))
    paths = [paths[i] for i in reversed(ii)]

    rules = []
    for path in paths:
        rule = "if "

        for p in path[:-1]:
            if rule != "if ":
                rule += " and "
            rule += str(p)
        rule += " then "
        if class_names is None:
            rule += "response: " + str(np.round(path[-1][0][0][0], 3))
        else:
            classes = path[-1][0][0]
            l = np.argmax(classes)
            rule += f"class: {class_names[l]} (probability: {np.round(100.0 * classes[l] / np.sum(classes), 2)}%)"
        rule += f" | Based on {path[-1][1]:,} samples"
        rules += [rule]

    return rules

rules = get_rules(dt, feature_cols, classnames)
for r in rules:
    print(r)


# Prediction:
dataset_test = pd.read_csv('Data/iis_coursework_test_dataset.csv')
# Label dataset into data and target:
feature_cols_test = ['Willingness_to_follow_order', 'Existence_of_physical_disabilities',
                'Willingness_to_help_peers', 'Running_towards_fire']
X_test = dataset_test[feature_cols_test] # Features
y_test = dataset_test.Does_student_pose_a_risk_during_evacuation
y_pred = dt.predict(X_test)

# Score metrics:
target_names = ['Low supervision required during evacuation', 'High supervision required during evacuation']
print(classification_report(y_test, y_pred, target_names=target_names))
print(confusion_matrix(y_test, y_pred))
print(brier_score_loss(y_test, y_pred))
print(log_loss(y_test,y_pred))

# predict probabilities
lr_probs = dt.predict_proba(X_test)
# keep probabilities for the positive outcome only
lr_probs = lr_probs[:, 1]
# calculate scores
lr_auc = roc_auc_score(y_test, lr_probs)
# summarize scores
print('Logistic: ROC AUC=%.3f' % (lr_auc))
# calculate roc curves
lr_fpr, lr_tpr, _ = roc_curve(y_test, lr_probs)
# plot the roc curve for the model
plt.plot(lr_fpr, lr_tpr, marker='.', label='Logistic')
# axis labels
plt.title('Measuring the skill of the tree: ROC curve')
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
# show the legend
plt.legend()
# show the plot
plt.savefig('roc_curve.pdf')


